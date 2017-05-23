package com.camlait.global.erp.service.validation;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.service.controllers.ProductService;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.collect.Lists;

@Component
public class TaxValidator implements Validator<Tax, Tax> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);

    @Override
    public ValidatorResult<Tax> validate(Tax toValidate) {
        final List<String> errors = Lists.newArrayList();
        if (toValidate == null) {
            errors.add("The tax object should not be null.");
        } else {
            LOGGER.info("Tax to add received. message = [{}]", toValidate.toJson());
            if (StringUtils.isNullOrEmpty(toValidate.getTaxCode())) {
                errors.add("The tax code should not be null or empty.");
            }
            if (StringUtils.isNullOrEmpty(toValidate.getTaxDescription())) {
                errors.add("The tax description should not be null or empty.");
            }
            if (!NumberUtils.isNumber(String.valueOf(toValidate.getPercentageValue()))) {
                errors.add("The percentage value should be a number.");
            }
        }
        return build(errors, toValidate);
    }

    @Override
    public ValidatorResult<Tax> build(List<String> errors, Tax result) {
        final ValidatorResult<Tax> vr = new ValidatorResult<Tax>();
        vr.setErrors(errors);
        vr.setResult(result);
        return vr;
    }
}
