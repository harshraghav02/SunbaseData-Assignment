package com.example.SunbaseAssignment.controller;

import com.example.SunbaseAssignment.model.Consumer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SunbaseController {
    @GetMapping("/")
    public String test() {
        return "login";
    }

    @PostMapping("/loginpage")
    public String processLogin(@RequestParam("email") String email, @RequestParam("password") String pass, HttpSession session) throws IOException, InterruptedException {

        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment_auth.jsp";
        String jsonData = String.format("{\"login_id\":\"%s\", \"password\":\"%s\"}", email, pass);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Content-Type", "application/json")
                .build();

        var cli = HttpClient.newBuilder().build();
        var res = cli.send(request, HttpResponse.BodyHandlers.ofString());
        String jsonString = res.body();
        String[] a = jsonString.split(":", 2);
        String token = a[1].substring(1, 41);
        session.setAttribute("token", token);
        return "welcome";
    }

    @GetMapping("/getcustomers")
    public String getConsumerData(HttpSession session, Model model) throws IOException, InterruptedException {
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "get_customer_list";
        String fullUrl = url + "?cmd=" + cmd;
        String token = (String) session.getAttribute("token");
        String t = "Bearer " + token;
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(fullUrl))
                .header("Authorization", t)
                .build();

        HttpClient cli = HttpClient.newBuilder().build();
        HttpResponse<String> res = cli.send(request, HttpResponse.BodyHandlers.ofString());
        if(res.statusCode() != 200)
        {
            return "redirect:/";
        }
        String jsonData = res.body();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(jsonData);
            List<Consumer> consumerList = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                Consumer person = mapper.readValue(node.toString(), Consumer.class);
                consumerList.add(person);
            }
            model.addAttribute("peopleList", consumerList);
        }
        catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return "display";
    }

    @GetMapping("/add")
    public String addCustomer() {
        return "add";
    }
    @PostMapping("/addcustomer")
    public String processContact(@RequestParam("firstname") String firstname,
                                 @RequestParam("lastname") String lastname,
                                 @RequestParam("street") String street,
                                 @RequestParam("address") String address,
                                 @RequestParam("city") String city,
                                 @RequestParam("state") String state,
                                 @RequestParam("email") String email,
                                 @RequestParam("phone") String phone,
                                 HttpSession session) throws IOException, InterruptedException {


        if (firstname.isEmpty() || lastname.isEmpty()) {
            return "Error: First Name or Last Name is missing";
        }
        String token = (String) session.getAttribute("token");
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "create";
        String fullUrl = url + "?cmd=" + cmd;
        String jsonData = String.format("{\"first_name\":\"%s\", \"last_name\":\"%s\", \"street\":\"%s\", \"address\":\"%s\", \"city\":\"%s\", \"state\":\"%s\", \"email\":\"%s\", \"phone\":\"%s\"}",
                firstname, lastname, street, address, city, state, email, phone);

        String authHeader = "Bearer " + token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Authorization", authHeader)
                .header("Content-Type", "application/json") // Add Content-Type header for JSON data
                .build();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 201) {
            return "redirect:/getcustomers";
        } else {
            return "redirect:/add";
        }
    }

    @PostMapping("/delete/{uuid}")
    public String deleteCustomer(@PathVariable("uuid") String uuid, HttpSession session) throws IOException, InterruptedException
    {
        String token = (String) session.getAttribute("token");
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "delete";
        String fullUrl = url + "?cmd=" + cmd + "&uuid=" + uuid;
        String authHeader = "Bearer " + token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Authorization", authHeader)
                .build();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        return "redirect:/getcustomers";
    }

    @GetMapping("/edit/{uuid}/{firstname}/{lastname}/{street}/{address}/{city}/{state}/{email}/{phone}")
    public String UpdateCustomer(@PathVariable("uuid") String uuid,@PathVariable("firstname") String firstname,
                                 @PathVariable("lastname") String lastname,@PathVariable("street") String street,@PathVariable("address") String address,
                                 @PathVariable("city") String city,@PathVariable("state") String state,@PathVariable("email") String email,@PathVariable("phone") String phone,
                                 Model model)
    {

        model.addAttribute("uuid",uuid);
        model.addAttribute("firstname",firstname);
        model.addAttribute("lastname",lastname);
        model.addAttribute("street",street);
        model.addAttribute("address",address);
        model.addAttribute("city",city);
        model.addAttribute("state",state);
        model.addAttribute("email",email);
        model.addAttribute("phone",phone);
        return "update";
    }

    @PostMapping("/processupdate/{uuid}")
    public String updateCustomer(@PathVariable("uuid") String uuid,  @RequestParam("firstname") String firstname, @RequestParam("lastname") String lastname,@RequestParam("street") String street,   @RequestParam("address") String address,  @RequestParam("city") String city,
                                 @RequestParam("state") String state,   @RequestParam("email") String email,  @RequestParam("phone") String phone,HttpSession session) throws IOException, InterruptedException
    {

        if(firstname.isEmpty() || lastname.isEmpty()){
            return "Error: First Name or Last Name is missing";
        }
        String token = (String) session.getAttribute("token");
        String url = "https://qa2.sunbasedata.com/sunbase/portal/api/assignment.jsp";
        String cmd = "update";
        String fullUrl = url + "?cmd=" + cmd + "&uuid=" + uuid;

        String jsonData = String.format("{\"first_name\":\"%s\", \"last_name\":\"%s\", \"street\":\"%s\", \"address\":\"%s\", \"city\":\"%s\", \"state\":\"%s\", \"email\":\"%s\", \"phone\":\"%s\"}",
                firstname, lastname, street, address, city, state, email, phone);

        String authHeader = "Bearer " + token;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Authorization", authHeader)
                .build();

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return "redirect:/getcustomers";
    }
}
