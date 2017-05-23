package com.camlait.global.erp.service.validation;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.service.controllers.ProductCategoryService;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class ProductValidator implements Validator<Product, Product> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryService.class);

    @Override
    public ValidatorResult<Product> validate(Product toValidate) {
        final List<String> errors = Lists.newArrayList();
        if (toValidate == null) {
            errors.add("The product should not be null");
        } else {
            LOGGER.info("Product to add received. message = [{}]", toValidate.toJson());
            if (StringUtils.isNullOrEmpty(toValidate.getProductCode())) {
                errors.add("The product code should not be null or empty");
            }
            if (StringUtils.isNullOrEmpty(toValidate.getProductDescription())) {
                errors.add("The product description should not be null or empty");
            }
            if (!NumberUtils.isNumber(toValidate.getDefaultUnitprice().toString())) {
                errors.add("The default unit price should be a numeric.");
            }
        }
        return build(errors, toValidate);
    }

    @Override
    public ValidatorResult<Product> build(List<String> errors, Product result) {
        final ValidatorResult<Product> vr = new ValidatorResult<Product>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
