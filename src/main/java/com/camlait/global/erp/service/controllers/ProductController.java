package com.camlait.global.erp.service.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.h2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.camlait.global.erp.delegate.document.DocumentManager;
import com.camlait.global.erp.delegate.product.ProductManager;
import com.camlait.global.erp.domain.document.business.Tax;
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.domain.product.ProductCategory;
import static com.camlait.global.erp.domain.util.SerializerUtil.toJson;

import com.camlait.global.erp.service.domain.ProductTaxes;
import com.camlait.global.erp.service.validation.ProductTaxValidator;
import com.camlait.global.erp.service.validation.ProductValidator;
import com.google.common.base.Joiner;

/**
 * Product catalog management service API.
 * <p>
 * Expose all the operations need to manage product catalog.
 * 
 * @author Martin Blaise Signe.
 */
@RestController
@RequestMapping(value = "global/v1/product")
public class ProductController {

    private final ProductManager productManager;
    private final ProductValidator productValidator;
    private final ProductTaxValidator productTaxVaildator;
    private final DocumentManager documentNanager;

    @Autowired
    public ProductController(ProductManager productManager, ProductValidator productValidator, ProductTaxValidator productTaxVaildator,
                             DocumentManager documentNanager) {
        this.productManager = productManager;
        this.productValidator = productValidator;
        this.productTaxVaildator = productTaxVaildator;
        this.documentNanager = documentNanager;
    }

    /**
     * Add a product to the catalog.
     * 
     * @param product Product to store.
     * @param categoryCode Product category code.
     * @return
     */
    @RequestMapping(value = "category/{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> productAdd(@RequestBody Product product, @PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body("The product category code should not be null or empty.");
        }
        final List<String> errors = productValidator.validate(product);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        final ProductCategory c = productManager.retrieveProductCategoryByCode(categoryCode);
        if (c == null) {
            return ResponseEntity.badRequest().body("No product category belongs to the category code " + categoryCode);
        }
        product.setCategory(c);
        final Product p = productManager.addProduct(product);
        return ResponseEntity.ok(p.toJson());
    }

    /**
     * Update a product in the catalog.
     * 
     * @param product Product to update.
     * @param productCode Target product code that need to be updated.
     * @return the updated product.
     */
    @RequestMapping(value = "/{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<String> productUpdate(@RequestBody Product product, @PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            return ResponseEntity.badRequest().body("The target product code should not be null or empty.");
        }
        final Product p = productManager.retrieveProductByCode(productCode);
        if (p == null) {
            return ResponseEntity.badRequest().body("The product with the code " + productCode + " does not exist.");
        }
        final Product toUpdate = product.merge(p);
        final List<String> errors = productValidator.validate(toUpdate);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        productManager.updateProduct(toUpdate);
        return ResponseEntity.ok(toUpdate.toJson());
    }

    /**
     * Retrieve a product from the catalog.
     * 
     * @param productCode Target product code that need to be retrieved.
     * @return the product that belongs to the provided code.
     */
    @RequestMapping(value = "/{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> productGet(@PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            return ResponseEntity.badRequest().body("The target product code should not be null or empty.");
        }
        final Product p = productManager.retrieveProductByCode(productCode);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The product with the code " + productCode + " does not exist in the catalog.");
        }
        return ResponseEntity.ok(p.toJson());
    }

    /**
     * Retrieve a product from the catalog base on the given keyword.
     * 
     * @param keyWord Keyword.
     * @param page Page number that need to be retrieved
     * @param size Number of items per page that need to be retrieved.
     * @return The collection of product that match with provided conditions.
     */
    @RequestMapping(value = "/keyWord/{keyWord}/page/{page}/size/{size}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> productGetByKeyWord(@PathVariable String keyWord, @PathVariable int page, @PathVariable int size) {
        if (StringUtils.isNullOrEmpty(keyWord)) {
            return ResponseEntity.badRequest().body("The keyword should not be null or empty.");
        }
        final Page<Product> p = productManager.retriveProducts(keyWord, new PageRequest(page, size));
        return ResponseEntity.ok(toJson(p.getContent()));
    }

    /**
     * Retrieves products from the catalog for a specific category.
     * 
     * @param categoryCode Target category.
     * @return All the product that belong to the provided category code.
     */
    @RequestMapping(value = "category/{categoryCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> productGetByCategory(@PathVariable String categoryCode) {
        if (StringUtils.isNullOrEmpty(categoryCode)) {
            return ResponseEntity.badRequest().body("The target category product code should not be null or empty.");
        }
        final List<Product> products = productManager.retriveProducts(null, null).getContent().stream()
                .filter(p -> p.getCategory().getProductCategoryCode().equals(categoryCode)).collect(Collectors.toList());
        return ResponseEntity.ok(toJson(products));
    }

    /**
     * Delete a product from the catalog.
     * 
     * @param productCode Target product code that need to be deleted.
     * @return
     */
    @RequestMapping(value = "/{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<String> productDelete(@PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            return ResponseEntity.badRequest().body("The target product code should not be null or empty.");
        }
        final Product p = productManager.retrieveProductByCode(productCode);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The product with the code " + productCode + " does not exist in the catalog.");
        }
        final Boolean result = productManager.removeProduct(p.getProductId());
        return result ? ResponseEntity.ok("The product " + p.getProductDescription() + " has been succesfully removed.")
                      : ResponseEntity.ok("The product " + p.getProductDescription() + " were not succesfully removed.");
    }

    /**
     * Associate a list of taxes to a given product.
     * 
     * @param productTaxes Object that group a product code to a list of tax code..
     * @return The product with associated taxes.
     */
    @RequestMapping(value = "/taxes", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> productTaxAssociation(@RequestBody ProductTaxes productTaxes) {
        final List<String> errors = productTaxVaildator.validate(productTaxes);
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Joiner.on('\n').join(errors));
        }
        final List<Tax> taxes = productTaxes.getTaxCodes().stream().map(c -> {
            return documentNanager.retrieveTaxByCode(c);
        }).collect(Collectors.toList());
        Product p = productManager.retrieveProductByCode(productTaxes.getProductCode());
        p.setTaxes(taxes);
        p = productManager.updateProduct(p);
        return ResponseEntity.ok(p.toJson());
    }
}
