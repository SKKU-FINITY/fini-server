package finity.fini.data;

import finity.fini.service.Bank.BankService;
import finity.fini.service.Product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final BankService bankService;
    private final ProductService productService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>>> 데이터 로더 시작 <<<<");

        // 은행 정보 동기화
        bankService.syncBanks();

        // 예/적금 상품 정보 동기화
//        productService.syncSavingProducts();
//        productService.syncDepositProducts();

        log.info(">>>> 데이터 로더 종료 <<<<");
    }
}