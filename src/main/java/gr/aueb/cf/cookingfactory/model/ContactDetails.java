package gr.aueb.cf.cookingfactory.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "contact_details")
public class ContactDetails extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;
    private String street;
    @Column(name = "street_number")
    private String streetNumber;
    private String postalCode;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;
}
