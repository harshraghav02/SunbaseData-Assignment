package com.example.SunbaseAssignment.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;


@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Consumer {
    String uuid;
    String first_name;
    String last_name;
    String street;
    String address;
    String city;
    String state;
    String email;
    String phone;

    //HELP DESERIALIZE THE JSON TO CLASS VARIABLE
    @JsonCreator
    public Consumer(@JsonProperty("uuid") String uuid,
                    @JsonProperty("first_name") String first_name,
                    @JsonProperty("last_name") String last_name,
                    @JsonProperty("street") String street,
                    @JsonProperty("address") String address,
                    @JsonProperty("city") String city,
                    @JsonProperty("state") String state,
                    @JsonProperty("email") String email,
                    @JsonProperty("phone") String phone){
        this.uuid = uuid;
        this.first_name=first_name;
        this.last_name = last_name;
        this.street = street;
        this.address = address;
        this.city = city;
        this.state = state;
        this.email = email;
        this.phone = phone;
    }
}
