package com.example.wealth.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Entity
@Data
public class Wealth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="mrhst_nm")
    private String mrhstNm;   // 가맹점 이름

    private String rdnmadr;   // 도로명 주소
    private String latitude;  // 위도 (API에서 String으로 옴)
    private String longitude; // 경도 (API에서 String으로 옴)

    @Column(name="ctprvn_nm")
    private String ctprvnNm;

    @Column(name="signgu_nm")
    private String signguNm;



    public Wealth() {}


    public Wealth(
            Long id,
            String mrhstNm,
            String rdnmadr,
            String latitude,
            String longitude,
            String ctprvnNm, //시도명
            String signguNm //시군구명
    ) {
        this.id = id;
        this.mrhstNm = mrhstNm;
        this.rdnmadr = rdnmadr;
        this.latitude = latitude;
        this.longitude = longitude;
        this.ctprvnNm = ctprvnNm;
        this.signguNm = signguNm;
    }
}
