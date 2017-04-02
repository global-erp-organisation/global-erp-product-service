package com.camlait.global.erp.service.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.document.DocumentManager;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.service.domain.ProductTaxes;
import com.camlait.global.erp.validation.Validator;
import com.google.common.collect.Lists;

@Component
public class ProductTaxValidator implements Validator<ProductTaxes> {

    private final ProductManager productManager;
    private final DocumentManager documentNanager;

    @Autowired
    public ProductTaxValidator(ProductManager productManager, DocumentManager documentNanager) {
        this.productManager = productManager;
        this.documentNanager = documentNanager;
    }

    @Override
    public List<String> validate(ProductTaxes toValidate) {
        final List<String> errors = Lists.newArrayList();
        if (toValidate == null) {
            errors.add("The productTaxes object should not be null");
        } else {
            if (StringUtils.isNullOrEmpty(toValidate.getProductCode())) {
                errors.add("The product code should not be empty or null");
            } else {
                final Product p = productManager.retrieveProductByCode(toValidate.getProductCode());
                if (p == null) {
                    errors.add("No product belongs to the code " + toValidate.getProductCode() + " has been found.");
                }
            }
            if (CollectionUtils.isNullOrEmpty(toValidate.getTaxCodes())) {
                errors.add("At least one tax code should be provided.");
            } else {
                toValidate.getTaxCodes().forEach(c -> {
                    if (StringUtils.isNullOrEmpty(c)) {
                        errors.add("A tax code should not be null or empty.");
                    } else {
                        final Tax t = documentNanager.retrieveTaxByCode(c);
                        if (t == null) {
                            errors.add("No Tax related to the code " + c + " has been found");
                        }
                    }
                });
            }
        }
        return errors;
    }
}
