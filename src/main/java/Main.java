import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=MJMhFmWtjyoGmACbmJSrQTML5YfI2YTwRJDhRck3";

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException, URISyntaxException {

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(15000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();

        HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
        request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse response = httpClient.execute(request);

        // вывод полученных заголовков
        Arrays.stream(response.getAllHeaders()).forEach(System.out::println);
        // чтение тела ответа
//        String body = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
//        System.out.println(body);

        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<Post> posts = mapper.readValue(
                response.getEntity().getContent(),
                new TypeReference<>() {
                }
        );

        posts.stream().forEach(System.out::println);

        String urlNASA = posts.get(0).getUrl();

        HttpGet requestNASA = new HttpGet(urlNASA);
        requestNASA.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());

        CloseableHttpResponse responseNASA = httpClient.execute(requestNASA);

        // вывод полученных заголовков
        Arrays.stream(responseNASA.getAllHeaders()).forEach(System.out::println);

        // Сохраните тело ответа в файл с именем части url
        String fileName = Paths.get(new URI(urlNASA).getPath()).getFileName().toString();
        try (FileOutputStream fos = new FileOutputStream("JupiterDarkSpot_JunoTT_1080.jpg")) {
            byte[] bodyNASA = responseNASA.getEntity().getContent().readAllBytes();
            // запись байтов в файл
            fos.write(bodyNASA, 0, bodyNASA.length);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        response.close();
        responseNASA.close();
        httpClient.close();
    }

}


