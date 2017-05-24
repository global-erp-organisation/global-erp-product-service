package com.camlait.global.erp.service.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.delegate.tax.TaxManager;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.service.controllers.TaxService;
import com.camlait.global.erp.service.domain.ProductTax;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class ProductTaxValidator implements Validator<ProductTax, Product> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxService.class);
    private final ProductManager productManager;
    private final TaxManager taxManager;

    @Autowired
    public ProductTaxValidator(ProductManager productManager, TaxManager taxManager) {
        this.productManager = productManager;
        this.taxManager = taxManager;
    }

    @Override
    public ValidatorResult<Product> validate(ProductTax toValidate) {
        final List<String> errors = Lists.newArrayList();
        Product p = null;
        if (toValidate == null) {
            errors.add("The productTaxes object should not be null");
        } else {
            LOGGER.info("ProductTax to add received. message = [{}]", toValidate.toJson());
            if (StringUtils.isNullOrEmpty(toValidate.getProductCode())) {
                errors.add("The product code should not be empty or null");
            } else {
                p = productManager.retrieveProductByCode(toValidate.getProductCode());
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
                        final Tax t = taxManager.retrieveTaxByCode(c);
                        if (t == null) {
                            errors.add("No Tax related to the code " + c + " has been found");
                        }
                    }
                });
            }
        }
        if (errors.isEmpty()) {
            final List<Tax> taxes = toValidate.getTaxCodes().stream().map(c -> {
                return taxManager.retrieveTaxByCode(c);
            }).collect(Collectors.toList());
            p.getTaxes().addAll(taxes);
        }
        return build(errors, p);
    }

    @Override
    public ValidatorResult<Product> build(List<String> errors, Product result) {
        final ValidatorResult<Product> vr = new ValidatorResult<Product>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
