package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.OrderItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.OrderItemRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    AuthUtil authUtil;

    @Value("${project.image}")
    private String path;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();
        for (Product value : products) {
            if (value.getProductName().equals(productDTO.getProductName())) {
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent) {
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategory(category);
            product.setUser(authUtil.loggedInUser());
            double specialPrice = product.getPrice() -
                    ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        } else {
            throw new APIException("Product already exist!!");
        }
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String keyword, String category) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));
        Specification<Product> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("productName")),
                            "%" + keyword.toLowerCase() + "%"));
        }

        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("category").get("categoryName")),
                            "%" + category.toLowerCase() + "%"));
        }

        Page<Product> pageProducts = productRepository.findAll(spec, pageDetails);
        return toProductResponse(pageProducts);
    }
    @Override
    public ProductResponse getAllProductsForSeller(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));

        Long userId = authUtil.loggedInUserId();
        Page<Product> pageProducts = productRepository.findByUser_UserId(userId, pageDetails);
        return toProductResponse(pageProducts);
    }

    private String constructImageUrl(String imageName) {
        return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    } // get url for image with local host

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category", "categoryId", categoryId));

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));
        Page<Product> pageProducts = productRepository.findByCategory(category, pageDetails);

        List<Product> products = pageProducts.getContent();

        if (products.isEmpty()) {
            throw new APIException(category.getCategoryName() + " category does not have any products");
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));
        Page<Product> pageProducts = productRepository.findByProductNameContainingIgnoreCase(keyword, pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty()) {
            throw new APIException("Products not found with keyword: " + keyword);
        }

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (orderItemRepository.existsByProduct(product)) {
            throw new APIException("Product cannot be deleted because it is linked to existing orders.");
        }

        // DELETE
        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.delete(product);
        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        String fileName = fileService.uploadImage(path, image); // path is the folder name
        productFromDb.setImage(fileName);

        Product updatedProduct = productRepository.save(productFromDb);
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }

    private ProductResponse toProductResponse(Page<Product> pageProducts) {
        List<ProductDTO> productDTOS = pageProducts.getContent().stream()
                .map(product -> {
                    ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);
                    productDTO.setImage(constructImageUrl(product.getImage())); // save with local host attached
                    return productDTO;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }

    private Sort buildSort(String sortBy, String sortOrder) {
        String property = (sortBy == null || sortBy.isBlank()) ? "price" : sortBy;

        return "asc".equalsIgnoreCase(sortOrder)
                ? Sort.by(property).ascending()
                : Sort.by(property).descending();
    }

    @Override
    public ProductResponse getAllProductsForAdmin(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, buildSort(sortBy, sortOrder));
        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        List<Product> products = pageProducts.getContent();
        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();



        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageProducts.getNumber());
        productResponse.setPageSize(pageProducts.getSize());
        productResponse.setTotalElements(pageProducts.getTotalElements());
        productResponse.setTotalPages(pageProducts.getTotalPages());
        productResponse.setLastPage(pageProducts.isLast());
        return productResponse;
    }
}
