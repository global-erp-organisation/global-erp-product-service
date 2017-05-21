package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class ProductValidator implements Validator<Product, Product> {

    private final ProductManager productManager;

    @Autowired
    public ProductValidator(ProductManager productManager) {
        this.productManager = productManager;
    }

    @Override
    public ValidatorResult<Product> validate(Product toValidate) {
        final List<String> errors = Lists.newArrayList();
        if (toValidate == null) {
            errors.add("The product should not be null");
        } else {
            if (StringUtils.isNullOrEmpty(toValidate.getProductCode())) {
                errors.add("The product code should not be null or empty");
            } else {
                final Product p = productManager.retrieveProductByCode(toValidate.getProductCode());
                if (p != null) {
                    errors.add("A product with the code " + toValidate.getProductCode() + " already exist in the catalog.");
                }
            }
            if (StringUtils.isNullOrEmpty(toValidate.getProductDescription())) {
                errors.add("The product description should not be null or empty");
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
