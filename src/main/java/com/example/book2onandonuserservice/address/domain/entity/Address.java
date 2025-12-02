package com.example.book2onandonuserservice.address.domain.entity;


import com.example.book2onandonuserservice.global.converter.EncryptStringConverter;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_address_id")
    private Long addressId;

    @Column(name = "user_address_name", length = 20)
    @Size(max = 20)
    @NotNull
    private String userAddressName;

    @Column(name = "recipient", length = 50)
    private String recipient;

    @Convert(converter = EncryptStringConverter.class)
    @Column(name = "phone", length = 255)
    private String phone;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "user_address", length = 255)
    @Convert(converter = EncryptStringConverter.class)
    @NotNull
    private String userAddress;

    @Column(name = "user_address_detail", length = 255)
    @Convert(converter = EncryptStringConverter.class)
    private String userAddressDetail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @NotNull
    @Column(name = "user_address_default")
    private boolean isDefault;

    //더티체킹을 위한 매서드
    //주소 정보 수정
    public void updateAddressInfo(String userAddressName, String recipient, String phone,
                                  String zipCode, String userAddress, String userAddressDetail) {
        this.userAddressName = userAddressName;
        this.recipient = recipient;
        this.phone = phone;
        this.zipCode = zipCode;
        this.userAddress = userAddress;
        this.userAddressDetail = userAddressDetail;
    }

    //대표 주소 상태 변경
    public void changeDefaultAddress(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
