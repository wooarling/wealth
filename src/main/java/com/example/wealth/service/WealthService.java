package com.example.wealth.service;

import com.example.wealth.model.Wealth;
import com.example.wealth.repository.WealthRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;


@Service
@Profile("init")
public class WealthService implements CommandLineRunner {



    private static final Logger logger = LoggerFactory.getLogger(WealthService.class);
    private final WealthRepository wealthRepository;

    @Value("${api.serviceKey}")
    private String serviceKey;

    public WealthService(WealthRepository wealthRepository) {
        this.wealthRepository = wealthRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        final int MAX_THREADS = 5;       // 동시에 처리할 페이지 수
        final int NUM_OF_ROWS = 100;     // 페이지당 데이터 수
        final int TOTAL_PAGES = 2755;    // 27만 건 / 100개씩 = 약 2755 페이지

        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS); //동시에 처리할 쓰레드 수를 고정
        ConcurrentMap<String, Boolean> uniqueKeys = new ConcurrentHashMap<>(); // 중복 체크
        ObjectMapper objectMapper = new ObjectMapper();
        List<Future<Void>> futures = new ArrayList<>();

        for (int page = 1; page <= TOTAL_PAGES; page++) {
            final int currentPage = page;

            Future<Void> future = executor.submit(() -> { //각 페이지를 별도의 스레드에서 처리
                try {
                    // API 호출 URL 구성
                    StringBuilder urlBuilder = new StringBuilder(
                            "http://api.data.go.kr/openapi/tn_pubr_public_chil_wlfare_mlsv_api");
                    urlBuilder.append("?serviceKey=").append(URLEncoder.encode(serviceKey, StandardCharsets.UTF_8));
                    urlBuilder.append("&pageNo=").append(currentPage);
                    urlBuilder.append("&numOfRows=").append(NUM_OF_ROWS);
                    urlBuilder.append("&type=json");

                    URL url = new URL(urlBuilder.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "application/json");

                    int responseCode = conn.getResponseCode();
                    if (responseCode != 200) {
                        logger.error("페이지 {} API 호출 실패: {}", currentPage, responseCode);
                        return null;
                    }

                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader rd = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = rd.readLine()) != null) {
                            sb.append(line);
                        }
                    }

                    JsonNode root = objectMapper.readTree(sb.toString());
                    JsonNode items = root.path("response").path("body").path("items");

                    if (!items.isArray() || items.isEmpty()) {
                        logger.info("페이지 {} 데이터 없음", currentPage);
                        return null;
                    }

                    List<Wealth> saveList = new ArrayList<>();

                    for (JsonNode node : items) {
                        String mrhstNm = node.path("mrhstNm").asText(null);
                        String rdnmadr = node.path("rdnmadr").asText(null);
                        String ctprvnNm=node.path("ctprvnNm").asText(null);
                        String signguNm=node.path("signguNm").asText(null);
                        if (mrhstNm == null || rdnmadr == null||ctprvnNm==null||signguNm==null) continue;

                        // 중복 체크
                        String uniqueKey =
                                ctprvnNm + "_" +
                                        signguNm + "_" +
                                        mrhstNm + "_" +
                                        rdnmadr;
                        if (uniqueKeys.putIfAbsent(uniqueKey, true) != null) continue;

                        Wealth wealth = new Wealth();
                        wealth.setMrhstNm(mrhstNm);
                        wealth.setRdnmadr(rdnmadr);
                        wealth.setCtprvnNm(ctprvnNm);
                        wealth.setSignguNm(signguNm);
                        wealth.setLatitude(node.path("latitude").asText(null));
                        wealth.setLongitude(node.path("longitude").asText(null));

                        saveList.add(wealth);
                    }

                    // 페이지 단위 저장
                    if (!saveList.isEmpty()) {
                        wealthRepository.saveAll(saveList);
                        logger.info("페이지 {} 저장 완료, 데이터 수: {}", currentPage, saveList.size());
                    }

                } catch (Exception e) {
                    logger.error("페이지 {} 처리 중 오류 발생", currentPage, e);
                }
                return null;
            });

            futures.add(future);
        }

        // 모든 스레드 작업 완료 대기
        for (Future<Void> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                logger.error("스레드 작업 중 예외 발생", e);
            }
        }

        executor.shutdown();
        logger.info("모든 페이지 처리 완료");
    }
    // 검색 메서드 추가
    public List<Wealth> search(String ctprvnNm, String signguNm) {
        if (ctprvnNm != null && !ctprvnNm.isEmpty()) {
            if (signguNm != null && !signguNm.isEmpty()) {
                return wealthRepository.findByCtprvnNmAndSignguNm(ctprvnNm, signguNm);
            } else {
                return wealthRepository.findByCtprvnNm(ctprvnNm);
            }
        }
        return List.of(); // 아무 선택 없으면 빈 리스트 반환
    }

}
