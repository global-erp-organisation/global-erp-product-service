package com.camlait.global.erp.service.controllers;

import static com.camlait.global.erp.domain.helper.SerializerHelper.toJson;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.util.StringUtils;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.delegate.tax.TaxManager;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.domain.product.TaxModel;
import com.camlait.global.erp.service.domain.CategoryTax;
import com.camlait.global.erp.service.domain.ProductTax;
import com.camlait.global.erp.validation.Validator;
import com.google.common.base.Joiner;

@CrossOrigin
@RestController
@RequestMapping(value = "global/v1/taxes/")
public class TaxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaxService.class);
    private final Validator<TaxModel> taxValidator;
    private final TaxManager taxManager;
    private final Validator<CategoryTax> categoryTaxValidator;
    private final Validator<ProductTax> productTaxValidator;
    private final ProductManager productManager;

    public TaxService(TaxManager taxManager, Validator<TaxModel> taxValidator, Validator<CategoryTax> categoryTaxValidator,
                      Validator<ProductTax> productTaxValidator, ProductManager productManager) {
        this.taxValidator = taxValidator;
        this.taxManager = taxManager;
        this.categoryTaxValidator = categoryTaxValidator;
        this.productTaxValidator = productTaxValidator;
        this.productManager = productManager;
    }

    /**
     * Add a tax to the catalog.
     * 
     * @param tax Tax to store.
     * @return
     */
    @RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> taxAdd(@RequestBody TaxModel tax ) {
        LOGGER.info("Tax to add received. message = [{}]", tax.toJson());
        final List<String> errors = taxValidator.validate(tax);
        if (!errors.isEmpty()) {
            LOGGER.error("Bad request. errors = [{}]", Joiner.on('\n').join(errors));
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        final Tax t = taxManager.addTax(TaxModel.fromTaxModel(tax));
        LOGGER.info("Tax successfully added. message = [{}]", t.toJson());
        return ResponseEntity.ok(t.toJson());
    }

    /**
     * Update a tax in the catalog.
     * 
     * @param tax Tax to update.
     * @param taxCode Target tax code that need to be updated.
     * @return the updated tax.
     */
    @RequestMapping(value = "/{taxCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PATCH)
    public ResponseEntity<String> taxUpdate(@RequestBody TaxModel tax, @PathVariable String taxCode) {
        if (StringUtils.isNullOrEmpty(taxCode)) {
            return ResponseEntity.badRequest().body("The target tax code should not be null or empty.");
        }
        final Tax t = taxManager.retrieveTaxByCode(taxCode);
        if (t == null) {
            return ResponseEntity.badRequest().body("The tax with the code " + taxCode + " does not exist.");
        }
        TaxModel toUpdate = tax.merge(TaxModel.fromTax(t));
        final List<String> errors = taxValidator.validate(toUpdate);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        Tax updated = TaxModel.fromTaxModel(tax);
        updated = updated.merge(t);
        updated = taxManager.updateTax(updated);
        return ResponseEntity.ok(updated.toJson());
    }

    /**
     * Retrieve a tax from the catalog.
     * 
     * @param taxCode Target tax code that need to be retrieved.
     * @return the tax that belongs to the provided code.
     */
    @RequestMapping(value = "/{taxCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> taxGet(@PathVariable String taxCode) {
        if (StringUtils.isNullOrEmpty(taxCode)) {
            return ResponseEntity.badRequest().body("The target Tax code should not be null or empty.");
        }
        final Tax t = taxManager.retrieveTaxByCode(taxCode);
        if (t == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The tax with the code " + taxCode + " does not exist in the catalog.");
        }
        return ResponseEntity.ok(t.toJson());
    }

    /**
     * Retrieve taxes from the catalog base on the given keyword.
     * 
     * @param keyWord Keyword.
     * @param page Page number that need to be retrieved
     * @param size Number of items per page that need to be retrieved.
     * @return The collection of taxes that match with provided conditions.
     */
    @RequestMapping(value = "/keyWord/{keyWord}/page/{page}/size/{size}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> taxGetByKeyWord(@PathVariable String keyWord, @PathVariable int page, @PathVariable int size) {
        if (StringUtils.isNullOrEmpty(keyWord)) {
            return ResponseEntity.badRequest().body("The keyword should not be null or empty.");
        }
        final Page<Tax> t = taxManager.retrieveTaxes(keyWord, new PageRequest(page, size));
        return ResponseEntity.ok(toJson(t.getContent()));
    }

    /**
     * Associate a list of taxes to a given product category.
     * 
     * @param categoryTax Object that group a product code to a list of tax code..
     * @return The product category with associated taxes.
     */
    @RequestMapping(value = "/category", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> categoryTaxAssociation(@RequestBody CategoryTax categoryTax) {
        final List<String> errors = categoryTaxValidator.validate(categoryTax);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        final List<Tax> taxes = categoryTax.getTaxCodes().stream().map(t -> {
            return taxManager.retrieveTaxByCode(t);
        }).collect(Collectors.toList());
        ProductCategory c = productManager.retrieveProductCategoryByCode(categoryTax.getCategoryCode());
        c.setTaxes(taxes);
        c = productManager.updateProductCategory(c);
        return ResponseEntity.ok(c.toJson());
    }

    /**
     * Associate a list of taxes to a given product.
     * 
     * @param productTaxes Object that group a product code to a list of tax code..
     * @return The product with associated taxes.
     */
    @RequestMapping(value = "/product", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> productTaxAssociation(@RequestBody ProductTax productTaxes) {
        final List<String> errors = productTaxValidator.validate(productTaxes);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        final List<Tax> taxes = productTaxes.getTaxCodes().stream().map(c -> {
            return taxManager.retrieveTaxByCode(c);
        }).collect(Collectors.toList());
        Product p = productManager.retrieveProductByCode(productTaxes.getProductCode());
        p.setTaxes(taxes);
        p = productManager.updateProduct(p);
        return ResponseEntity.ok(p.toJson());
    }

    /**
     * Delete a tax from the catalog.
     * 
     * @param taxCode Target product code that need to be deleted.
     * @return
     */
    @RequestMapping(value = "/{taxCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<String> taxDelete(@PathVariable String taxCode) {
        if (StringUtils.isNullOrEmpty(taxCode)) {
            return ResponseEntity.badRequest().body("The target tax code should not be null or empty.");
        }
        final Tax t = taxManager.retrieveTaxByCode(taxCode);
        if (t == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The tax with the code " + taxCode + " does not exist in the catalog.");
        }
        final Boolean result = taxManager.removeTax(t.getTaxId());
        return result ? ResponseEntity.ok("The product " + t.getTaxDescription() + " has been succesfully removed.")
                      : ResponseEntity.ok("The product " + t.getTaxDescription() + " were not succesfully removed.");
    }
}
