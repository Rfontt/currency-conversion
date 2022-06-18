package com.microservise.currencyconversion.controller;

import com.microservise.currencyconversion.entities.CurrencyConversion;
import com.microservise.currencyconversion.proxy.CurrencyExchangeProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;

@RestController
@RequestMapping("/currency-conversion")
public class CurrencyConversionController {
    @Autowired
    private CurrencyExchangeProxy proxy;

    @GetMapping("/from/{from}/to/{to}/quantity/{quantity}")
    public ResponseEntity calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
            ) {

        HashMap<String, String> uriVariables = new HashMap<>();

        uriVariables.put("from", from);
        uriVariables.put("to", to);

        try {

           ResponseEntity<CurrencyConversion> response = new RestTemplate().getForEntity(
             "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                   CurrencyConversion.class,
                   uriVariables
           );

            CurrencyConversion bodyOfResponse = response.getBody();

            CurrencyConversion currencyConversion = new CurrencyConversion(
                   bodyOfResponse.getId(),
                   from,
                   to,
                   quantity,
                   bodyOfResponse.getConversionMultiple(),
                   quantity.multiply(bodyOfResponse.getConversionMultiple()),
                   bodyOfResponse.getEnvironment()
            );

            return new ResponseEntity(currencyConversion, HttpStatus.OK);
        } catch (Exception error) {
            return new ResponseEntity("Currency Conversion not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("proxy/from/{from}/to/{to}/quantity/{quantity}")
    public ResponseEntity calculateCurrencyConversionWithProxy(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity
    ) {

        try {
            CurrencyConversion bodyOfResponse = proxy.retrieveExchangeValue(from, to);

            CurrencyConversion currencyConversion = new CurrencyConversion(
                    bodyOfResponse.getId(),
                    from,
                    to,
                    quantity,
                    bodyOfResponse.getConversionMultiple(),
                    quantity.multiply(bodyOfResponse.getConversionMultiple()),
                    bodyOfResponse.getEnvironment()
            );

            return new ResponseEntity(currencyConversion, HttpStatus.OK);
        } catch (Exception error) {
            return new ResponseEntity("Currency Conversion not found", HttpStatus.NOT_FOUND);
        }
    }
}
