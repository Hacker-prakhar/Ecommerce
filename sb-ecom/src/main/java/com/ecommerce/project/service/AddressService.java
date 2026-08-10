package com.ecommerce.project.service;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AddressService {
     AddressDTO createAddress(AddressDTO addressDTO, User user);
List<AddressDTO> getAddresses();
AddressDTO getAddressesById(Long addressId);
List<AddressDTO> getUserAddress(User user);
AddressDTO updateAddress(Long addressId,  AddressDTO addressDTO);
     AddressDTO deleteaddress(Long addressId);
}
