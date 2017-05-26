package com.camlait.global.erp.service.validation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.service.controllers.ProductCategoryService;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class CategoryValidator implements Validator<ProductCategory, ProductCategory> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCategoryService.class);

    private final ProductManager productManager;

    @Autowired
    public CategoryValidator(ProductManager productManager) {
        this.productManager = productManager;
    }

    @Override
    public ValidatorResult<ProductCategory> validate(ProductCategory toValidate) {
        final List<String> errors = Lists.newArrayList();
        if (toValidate == null) {
            errors.add("The product category object should not be null.");
        } else {
            LOGGER.info("Product category to add received. message = [{}]", toValidate.toJson());

            if (!StringUtils.isNullOrEmpty(toValidate.getParentCategoryCode())) {
                final ProductCategory parent = productManager.retrieveProductCategory(toValidate.getParentCategoryCode());
                if (parent == null) {
                    errors.add("The product category code with code " + toValidate.getParentCategoryCode() + " does not exist in the catalog.");
                } else {
                    if (parent.isDetail()) {
                        errors.add("The product category code with code " + toValidate.getParentCategoryCode() + " is not a regroupment category.");
                    } else {
                        toValidate.addParent(parent);
                    }
                }
            }

            if (StringUtils.isNullOrEmpty(toValidate.getProductCategoryCode())) {
                errors.add("The product category code should not be null or empty.");
            }
            if (StringUtils.isNullOrEmpty(toValidate.getCategoryDescription())) {
                errors.add("The product category description should not be null or empty.");
            }
        }
        return build(errors, toValidate);
    }

    @Override
    public ValidatorResult<ProductCategory> build(List<String> errors, ProductCategory result) {
        final ValidatorResult<ProductCategory> vr = new ValidatorResult<ProductCategory>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
