package jp.co.pmacmobile.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jp.co.pmacmobile.common.annotation.ResponseResult;
import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.domain.dto.ContractProgressDisplayDTO;
import jp.co.pmacmobile.domain.dto.ContractProgressSearchConditionDTO;
import jp.co.pmacmobile.domain.service.ContractProgressService;

/**
 * 新契約申込み進捗確認コントローラ
 *
 * <ul>新契約申込み進捗確認検索機能</ul>
 *
 * @author hitachi
 *
 */
@ResponseResult
@RestController
@RequestMapping("/contractProgress")
public class ContractProgressController {

    /**
     * 新契約申込み進捗確認検索サービスをインジェクションする
     */
    @Autowired
    ContractProgressService contractProgressService;

    /**
     * 新契約申込み進捗確認検索処理
     *
     * @return 新契約申込み進捗確認検索結果DTO
     * @throws MobileException
     *
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public  List<ContractProgressDisplayDTO> search(@RequestBody ContractProgressSearchConditionDTO dto) {
        // 結果返却
        return this.contractProgressService.getContractProgressList(dto);
    }

}
