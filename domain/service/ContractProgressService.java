package jp.co.pmacmobile.domain.service;

import java.util.List;

import jp.co.pmacmobile.domain.dto.ContractProgressDisplayDTO;
import jp.co.pmacmobile.domain.dto.ContractProgressSearchConditionDTO;

/**
 * 新契約申込み進捗確認サービス
 *
 * <ul>新契約申込み進捗確認検索機能</ul>
 *
 * @author hitachi
 *
 */
public interface ContractProgressService {

    /**
     * 新契約申込み進捗確認一覧リストを取得する。
     *
     * @param dairitenCD
     *            代理店コード
     * @return 一覧リスト
     */
    List<ContractProgressDisplayDTO> getContractProgressList(ContractProgressSearchConditionDTO dto);

}
