package com.alex.bank.utils.currencyConvert;

import com.alex.bank.models.account.CurrencyAccount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class Converter {

    private final RestTemplate restTemplate;

    public BigDecimal getConvertToCurrency(CurrencyAccount from, CurrencyAccount to, BigDecimal amount) {
        log.info("Converting {} {} to {}", amount, from, to);
        String url = "https://api.frankfurter.app/latest?from=" + from + "&to=" + to;
        CurrencyResponse response = restTemplate.getForObject(url, CurrencyResponse.class);
        if (response == null || response.getRates() == null
                || response.getRates().get(to.toString()) == null) {
            log.error("Failed to get exchange rate from {} to {}", from, to);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error with getting rates");
        }
        BigDecimal result = amount.multiply(response.getRates().get(to.toString()));
        log.info("Converted {} {} -> {} {}", amount, from, result, to);
        return result;
    }
}
