package com.example.sahldarbak.Controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/travel-request")
    public String travelRequest() {
        return "travel-request";
    }



    @GetMapping("/city-plan")
    public String cityPlan() {
        return "city-plan";
    }


    @GetMapping("/trip")
    public String trip() {
        return "trip";
    }

    @GetMapping("/trip-start")
    public String tripStart() {
        return "trip-start";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/city")
    public String city() {
        return "city";
    }

    @GetMapping("/checkin")
    public String checkin() {
        return "checkin";
    }

    @GetMapping("/travel-match")
    public String travelMatch() {
        return "travel-match";
    }

    @GetMapping("/invites")
    public String invites() {
        return "invites";
    }

    @GetMapping("/connections")
    public String connections() {
        return "connections";
    }

    @GetMapping("/blocked")
    public String blocked() {
        return "blocked";
    }

    @GetMapping("/community")
    public String community() {return "community";}

    @GetMapping("/travel-tools")
    public String travelTools() {return "travel-tools";}

    @GetMapping("/country-compare")
    public String countryCompare() {return "country-compare";}

}
