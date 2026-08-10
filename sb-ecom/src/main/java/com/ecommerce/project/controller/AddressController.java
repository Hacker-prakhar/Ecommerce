package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {
    @Autowired
    private AddressService addressService;
    @Autowired
    AuthUtil authUtil;
    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO saved;
        User user = authUtil.loggedInUser();
        saved = addressService.createAddress(addressDTO,user);
        return new ResponseEntity<AddressDTO>(saved, HttpStatus.CREATED);
    }
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
        List<AddressDTO> saved=addressService.getAddresses();
        return ResponseEntity.ok(saved);
    }
    @GetMapping({"/addresses/{addressId}", "/{addressId}/addresses"})
    public ResponseEntity<AddressDTO>  getAddresses(@PathVariable("addressId") Long addressId){
        AddressDTO addressDTO = addressService.getAddressesById(addressId) ;
        return new ResponseEntity<AddressDTO>(addressDTO, HttpStatus.CREATED);
    }
    @PutMapping({"/addresses/{addressId}", "/{addressId}/addresses"})
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable("addressId")Long addressId,@RequestBody AddressDTO addressDTO){
        AddressDTO saved;
        saved = addressService.updateAddress( addressId,addressDTO);
        return new ResponseEntity<AddressDTO>(saved, HttpStatus.OK);
    }
@DeleteMapping({"/addresses/{addressId}", "/{addressId}/addresses"})
    public ResponseEntity<AddressDTO> deleteAddress(@PathVariable("addressId")Long addressId){
        AddressDTO saved;
        saved = addressService.deleteaddress(addressId) ;
        return new ResponseEntity<>(saved, HttpStatus.OK);
}
}
