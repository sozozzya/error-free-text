package com.senla.errorfreetext.client;

import com.senla.errorfreetext.exception.SpellerClientException;
import com.senla.errorfreetext.model.Language;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class YandexSpellerClient implements SpellerClient {

    private static final Logger log = LoggerFactory.getLogger(YandexSpellerClient.class);
    private static final ParameterizedTypeReference<List<List<SpellError>>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public YandexSpellerClient(RestClient spellerRestClient) {
        this.restClient = spellerRestClient;
    }

    @Override
    public List<SpellResult> checkTexts(List<String> texts, Language language, int options) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        texts.forEach(text -> form.add("text", text));
        form.add("lang", language.toSpellerCode());
        form.add("options", String.valueOf(options));

        try {
            List<List<SpellError>> payload = restClient.post()
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new SpellerClientException(
                                "Yandex Speller returned HTTP " + response.getStatusCode().value()
                        );
                    })
                    .body(RESPONSE_TYPE);

            if (payload == null) {
                throw new SpellerClientException("Yandex Speller returned an empty response");
            }
            if (payload.size() != texts.size()) {
                throw new SpellerClientException(
                        "Yandex Speller returned " + payload.size() + " results for " + texts.size() + " fragments"
                );
            }

            List<SpellResult> results = new ArrayList<>(payload.size());
            for (List<SpellError> errors : payload) {
                results.add(new SpellResult(errors == null ? List.of() : errors));
            }
            return results;
        } catch (SpellerClientException ex) {
            log.error("Yandex Speller API error: {}", ex.getMessage());
            throw ex;
        } catch (RestClientException ex) {
            log.error("Yandex Speller request failed: {}", ex.getMessage());
            throw new SpellerClientException("Failed to call Yandex Speller", ex);
        }
    }
}
