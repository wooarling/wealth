package com.example.wealth.repository;

import com.example.wealth.model.Wealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WealthRepository extends JpaRepository<Wealth, Long> {

    // 시도 목록 조회 (중복 제거, 정렬)
    @Query("select distinct w.ctprvnNm from Wealth w order by w.ctprvnNm")
    List<String> findDistinctCtprvnNm();

    // 군구 목록 조회 (시도 기준)
    @Query("select distinct w.signguNm from Wealth w where w.ctprvnNm = :ctprvnNm order by w.signguNm")
    List<String> findSignguByCtprvnNm(@Param("ctprvnNm") String ctprvnNm);

    // 시도 + 군구 + 가맹점명(부분 검색) 검색
    List<Wealth> findByCtprvnNmAndSignguNmAndMrhstNmContaining(
            String ctprvnNm, String signguNm, String mrhstNm);

    // 시도 + 군구 기준 검색
    List<Wealth> findByCtprvnNmAndSignguNm(String ctprvnNm, String signguNm);

    // 시도 기준 + 가맹점명 검색
    List<Wealth> findByCtprvnNmAndMrhstNmContaining(String ctprvnNm, String mrhstNm);

    //시도 기준만 검색
    List<Wealth> findByCtprvnNm(String ctprvnNm);

    // 7️가맹점명만 부분 검색
    List<Wealth> findByMrhstNmContaining(String mrhstNm);
}


