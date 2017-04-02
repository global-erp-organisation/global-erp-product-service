package com.camlait.global.erp.service.validation;

import java.util.List;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.validation.Validator;

@Component
public class ProductValidator implements Validator<Product> {

    private final ProductManager productManager;

    @Autowired
    public ProductValidator(ProductManager productManager) {
        this.productManager = productManager;
    }

    @Override
    public List<String> validate(Product toValidate) {
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
        return errors;
    }

}
