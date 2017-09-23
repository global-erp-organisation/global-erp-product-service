package com.camlait.global.erp.service.controllers;

import static com.camlait.global.erp.domain.helper.SerializerHelper.toJson;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
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
import com.camlait.global.erp.domain.product.Product;
import com.camlait.global.erp.domain.product.ProductCategory;
import com.camlait.global.erp.service.controllers.BaseController;
import com.camlait.global.erp.validation.Validator;
import com.camlait.global.erp.validation.ValidatorResult;
import com.google.common.base.Joiner;

/**
 * Product catalog management service API.
 * <p>
 * Expose all the operations need to manage product catalog.
 * 
 * @author Martin Blaise Signe.
 */

@RefreshScope
@CrossOrigin
@RestController
@RequestMapping(value = "global/v1/products/")
public class ProductService extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final ProductManager productManager;
    private final Validator<Product, Product> productValidator;

    @Autowired
    public ProductService(ProductManager productManager, Validator<Product, Product> productValidator) {
        this.productManager = productManager;
        this.productValidator = productValidator;
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
            LOGGER.error("The product category code should not be null or empty.");
            return ResponseEntity.badRequest().body(genericMessage("The product category code should not be null or empty."));
        }
        final Product exist = productManager.retrieveProduct(product.getProductCode());
        if (exist != null) {
            LOGGER.error("The product with the code {} already exist in the catalog.", product.getProductCode());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(genericMessage("The product with code " + product.getProductCode() + " already exist in the catalog."));
        }
        final ProductCategory cp = productManager.retrieveProductCategory(categoryCode);
        if (cp == null) {
            LOGGER.error("The product category code {} were not find in the catalog.", categoryCode);
            return ResponseEntity.badRequest().body(genericMessage("The product category code " + categoryCode + " were not find in the catalog."));
        }
        if (cp.isTotal()) {
            LOGGER.error("The product category code {} is a regroupment category. Only detail category should be applied to a product.", categoryCode);
            return ResponseEntity.badRequest()
                    .body(genericMessage("The product category code " + categoryCode + " is a regroupment category. Only detail catgory should be applied to a product."));
        }
        product.setCategory(cp);
        final ValidatorResult<Product> result = productValidator.validate(product);
        final List<String> errors = result.getErrors();
        if (!errors.isEmpty()) {
            LOGGER.error("Bad request. errors = [{}]", Joiner.on('\n').join(errors));
            return ResponseEntity.badRequest().body(genericMessage(errors.toString()));
        }
        final Product p = productManager.addProduct(product);
        LOGGER.info("Product successfully added. message = [{}]", p.toJson());
        return ResponseEntity.status(HttpStatus.CREATED).body(p.toJson());
    }

    /**
     * Update a product in the catalog.
     * 
     * @param product Product to update.
     * @param productCode Target product code that need to be updated.
     * @return the updated product.
     */
    @RequestMapping(value = "{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.PUT)
    public ResponseEntity<String> productUpdate(@RequestBody Product product, @PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            LOGGER.error("The target product code should not be null or empty.");
            return ResponseEntity.badRequest().body(genericMessage("The target product code should not be null or empty."));
        }
        final Product p = productManager.retrieveProduct(productCode);
        if (p == null) {
            LOGGER.error("The product with code {} not found in the catalog.", productCode);
            return ResponseEntity.badRequest().body(genericMessage("The product with the code " + productCode + " does not exist."));
        }
        final Product toUpdate = product.merge(p);
        final ValidatorResult<Product> result = productValidator.validate(toUpdate);
        final List<String> errors = result.getErrors();
        if (!errors.isEmpty()) {
            LOGGER.error("Bad request. errors = [{}]", Joiner.on('\n').join(errors));
            return ResponseEntity.badRequest().body(genericMessage(errors.toString()));
        }
        productManager.updateProduct(toUpdate);
        LOGGER.info("Product successfully updated. message = [{}]", toUpdate.toJson());
        return ResponseEntity.ok(toUpdate.toJson());
    }

    /**
     * Retrieve a product from the catalog.
     * 
     * @param productCode Target product code that need to be retrieved.
     * @return the product that belongs to the provided code.
     */
    @RequestMapping(value = "{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> productGet(@PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            LOGGER.error("The product code should not be null or empty.");
            return ResponseEntity.badRequest().body(genericMessage("The target product code should not be null or empty."));
        }
        final Product p = productManager.retrieveProduct(productCode);
        if (p == null) {
            LOGGER.error("The product with code {} not found in the catalog.", productCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(genericMessage("The product with the code " + productCode + " does not exist in the catalog."));
        }
        LOGGER.info("Product successfully retrieved. message = [{}]", p.toJson());
        return ResponseEntity.ok(p.toJson());
    }

    /**
     * Retrieve products from the catalog base on the given keyword.
     * 
     * @param keyWord Keyword.
     * @return The collection of products that match with provided conditions.
     */
    @RequestMapping(value = {"keyword", "keyword/{keyWord}"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.GET)
    public ResponseEntity<String> productGetByKeyWord(@PathVariable(required = false) Optional<String> keyWord) {
        final String present = keyWord.isPresent() ? keyWord.get() : null;
        final List<Product> p = productManager.retrieveProducts(present);
        return ResponseEntity.ok(toJson(p));
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
            return ResponseEntity.badRequest().body(genericMessage("The target category product code should not be null or empty."));
        }
        final List<Product> products = productManager.retrieveProductByCategory(categoryCode);
        return ResponseEntity.ok(toJson(products));
    }

    /**
     * Delete a product from the catalog.
     * 
     * @param productCode Target product code that need to be deleted.
     * @return
     */
    @RequestMapping(value = "{productCode}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, method = RequestMethod.DELETE)
    public ResponseEntity<String> productDelete(@PathVariable String productCode) {
        if (StringUtils.isNullOrEmpty(productCode)) {
            return ResponseEntity.badRequest().body(genericMessage("The target product code should not be null or empty."));
        }
        final Product p = productManager.retrieveProduct(productCode);
        if (p == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(genericMessage("The product with the code " + productCode + " does not exist in the catalog."));
        }
        final Boolean result = productManager.removeProduct(p.getProductId());
        return result ? ResponseEntity.ok(genericMessage("The product " + p.getProductDescription() + " has been succesfully removed."))
                      : ResponseEntity.ok(genericMessage("The product " + p.getProductDescription() + " were not succesfully removed."));
    }
}
