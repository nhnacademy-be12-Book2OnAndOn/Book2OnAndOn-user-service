package com.example.book2onandonuserservice.address.domain.entity;


import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "UserAddress")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_address_id")
    private Long addressId;

    @Column(name = "user_address_name", length = 20)
    @Size(max = 20)
    @NotNull
    private String userAddressName;

    @Column(name = "user_address", length = 100)
    @Size(max = 100)
    @NotNull
    private String userAddress;

    @Column(name = "user_address_detail", length = 100)
    @Size(max = 100)
    private String userAddressDetail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

}
