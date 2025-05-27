package com.rabbiter.fm.controller;

import com.rabbiter.fm.common.enums.ErrorMsg;
import com.rabbiter.fm.model.AddressModel;
import com.rabbiter.fm.service.AddressService;
import com.rabbiter.fm.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/info")
    public ResultVo getAddress(@RequestParam("userId") Long userId,
            @RequestParam(value = "id", required = false) Long id) {
        if (null == id) {
            return ResultVo.success(addressService.getAddressByUser(userId));
        } else {
            return ResultVo.success(addressService.getAddressById(id, userId));
        }
    }

    @PostMapping("/add")
    public ResultVo addAddress(@RequestBody AddressModel addressModel) {
        if (addressService.addAddress(addressModel)) {
            return ResultVo.success(addressModel);
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("/update")
    public ResultVo updateAddress(@RequestBody AddressModel addressModel) {
        if (addressService.updateAddress(addressModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }

    @PostMapping("/delete")
    public ResultVo deleteAddress(@RequestBody AddressModel addressModel) {
        if (addressService.deleteAddress(addressModel)) {
            return ResultVo.success();
        }
        return ResultVo.fail(ErrorMsg.SYSTEM_ERROR);
    }
}
