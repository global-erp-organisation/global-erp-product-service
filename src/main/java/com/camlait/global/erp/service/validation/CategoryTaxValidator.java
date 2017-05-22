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
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.service.controllers.TaxService;
import com.camlait.global.erp.service.domain.CategoryTax;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class CategoryTaxValidator implements Validator<CategoryTax, ProductCategory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxService.class);
    private final ProductManager productManager;
    private final TaxManager taxManager;

    @Autowired
    public CategoryTaxValidator(ProductManager productManager, TaxManager taxManager) {
        this.productManager = productManager;
        this.taxManager = taxManager;
    }

    @Override
    public ValidatorResult<ProductCategory> validate(CategoryTax toValidate) {
        final List<String> errors = Lists.newArrayList();
        ProductCategory pc = null;
        if (toValidate == null) {

        } else {
            LOGGER.info("CategoryTax to add received. message = [{}]", toValidate.toJson());
            if (StringUtils.isNullOrEmpty(toValidate.getCategoryCode())) {
                errors.add("The product category code should not be empty or null");
            } else {
                pc = productManager.retrieveProductCategoryByCode(toValidate.getCategoryCode());
                if (pc == null) {
                    errors.add("No product category belongs to the code " + toValidate.getCategoryCode() + " has been found.");
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
            if (errors.isEmpty()) {
                final List<Tax> taxes = toValidate.getTaxCodes().stream().map(c -> {
                    return taxManager.retrieveTaxByCode(c);
                }).collect(Collectors.toList());
                pc.addCategoryToTax(taxes);
            }
        }
        return build(errors, pc);
    }

    @Override
    public ValidatorResult<ProductCategory> build(List<String> errors, ProductCategory result) {
        final ValidatorResult<ProductCategory> vr = new ValidatorResult<ProductCategory>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
