package com.camlait.global.erp.service.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.camlait.global.erp.service.domain.CategoryTax;
import com.camlait.global.erp.validation.Validator;

@Component
public class CategoryTaxValidator implements Validator<CategoryTax> {

	@Override
	public List<String> validate(CategoryTax toValidate) {
		final List<String> errors = new ArrayList<>();
		return errors;
	}

}
