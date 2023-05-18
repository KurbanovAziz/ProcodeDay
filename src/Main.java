import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Request;
import lombok.Data;
import okhttp3.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Data
class Student {
    private String name;
    private String phone;
    private String githubUrl;
}

@Data
class Country {
    private String country;
    private List<String> cities;
    private String citiesCount;
}

public class Main {
    private static final String GET_URL = "https://procodeday-01.herokuapp.com/meet-up/get-country-list";
    private static final String POST_URL = "https://procodeday-01.herokuapp.com/meet-up/post-request";

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            List<Country> countries = getCountries();

            Map<String, List<String>> groupedCities = groupCitiesByCountry(countries);

            sortCities(groupedCities);

            List<Country> result = countCitiesPerCountry(groupedCities);

            Student student = fillStudentData();

            postResult(student, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Country> getCountries() throws IOException {
        Request request = new Request.Builder()
                .url(GET_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            String responseBody = response.body().string();
            return Arrays.asList(mapper.readValue(responseBody, Country[].class));
        }
    }

    private static Map<String, List<String>> groupCitiesByCountry(List<Country> countries) {
        return countries.stream()
                .collect(Collectors.groupingBy(Country::getCountry,
                        Collectors.mapping(Country::getCity, Collectors.toList())));
    }

    private static void sortCities(Map<String, List<String>> groupedCities) {
        groupedCities.values().forEach(Collections::sort);
    }

    private static List<Country> countCitiesPerCountry(Map<String, List<String>> groupedCities) {
        return groupedCities.entrySet().stream()
                .map(entry -> {
                    Country country = new Country();
                    country.setCountry(entry.getKey());
                    country.setCities(entry.getValue());
                    country.setCitiesCount(String.valueOf(entry.getValue().size()));
                    return country;
                })
                .collect(Collectors.toList());
    }

    private static Student fillStudentData() {
        Student student = new Student();
        student.setName("Abdul-Aziz");
        student.setPhone("+996556385802");
        student.setGithubUrl("https://github.com/johndoe");
        return student;
    }

    private static void postResult(Student student, List<Country> result) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("student", student);
        requestBody.put("result", result);

        RequestBody body = RequestBody.create(PageAttributes.MediaType.parse("application/json"),
                mapper.writeValueAsString(requestBody));

        Request request = new Request.Builder()
                .url(POST_URL)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }

            System.out.println("POST request successful");
        }
    }
}
