package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.Order;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private AddressRepository addressRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = new Address(
                addressDTO.getStreet(),
                addressDTO.getBuildingName(),
                addressDTO.getCity(),
                addressDTO.getState(),
                addressDTO.getCountry(),
                addressDTO.getPincode()
        );
        address.setUser(user);
        user.addAddress(address);
        Address savedAddress = addressRepo.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addresses = addressRepo.findAll();
        List<AddressDTO> addressDTOs = new ArrayList<>();
        for (Address address : addresses) {
            AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
            addressDTOs.add(addressDTO);
        }
        return addressDTOs;
    }

    public AddressDTO getAddressesById(Long addressId) {
        Address address = addressRepo.findById(addressId).orElse(null);
        if (address == null) {
            throw new APIException("Address not found");
        }
        AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getUserAddress(User user) {
        List<Address> addresses = user.getAddresses();
        List<AddressDTO> addressDTOs = new ArrayList<>();
        for (Address address : addresses) {
            AddressDTO addressDTO = modelMapper.map(address, AddressDTO.class);
            addressDTOs.add(addressDTO);
        }
        return addressDTOs;
    }
    @Override
    public AddressDTO updateAddress(Long addressId,  AddressDTO addressDTO){
        Address address = addressRepo.findById(addressId).orElse(null);
        if (address == null) {
            throw new APIException("Address not found");
        }
        address.setStreet(addressDTO.getStreet());
        address.setBuildingName(addressDTO.getBuildingName());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setPincode(addressDTO.getPincode());
        addressRepo.save(address);
        return modelMapper.map(address, AddressDTO.class);
    }
    @Override
    @Transactional
    public AddressDTO deleteaddress(Long addressId) {
        Address address = addressRepo.findById(addressId).orElse(null);
        if (address == null) {
            throw new APIException("No such address exist") ;
        }
        List<Order> orders = orderRepo.findByAddress_AddressId(addressId);
        orders.forEach(order -> {
            order.setAddress(null);
        });
        orderRepo.saveAll(orders);
        addressRepo.delete(address);
        return modelMapper.map(address, AddressDTO.class);
    }
}
