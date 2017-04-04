package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.domain.product.ProductCategoryModel;
import com.camlait.global.erp.validation.Validator;

@Component
public class CategoryModelValidator implements Validator<ProductCategoryModel> {

    @Override
    public List<String> validate(ProductCategoryModel toValidate) {
        return null;
    }

}
