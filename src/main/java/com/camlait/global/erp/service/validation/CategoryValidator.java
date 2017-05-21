package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;

@Component
public class CategoryValidator implements Validator<ProductCategory, ProductCategory> {

    @Override
    public ValidatorResult<ProductCategory> validate(ProductCategory toValidate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValidatorResult<ProductCategory> build(List<String> errors, ProductCategory result) {
        // TODO Auto-generated method stub
        return null;
    }
}
