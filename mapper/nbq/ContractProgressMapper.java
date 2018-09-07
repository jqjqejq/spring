package jp.co.pmacmobile.domain.mapper.nbq;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jp.co.pmacmobile.domain.dto.ContractProgressSearchResultDTO;

/**
 * 新契約申込み進捗確認マップ
 *
 * <ul>新契約申込み進捗確認一覧リストを取得する</ul>
 * <ul>支部連携の所長チェック済フラグリストを取得する</ul>
 * <ul>（法人）エラーワーニングリストを取得する</ul>
 * <ul>処理日を取得する</ul>
 *
 * @author hitachi
 *
 */
@Mapper
public interface ContractProgressMapper {

    /**
     * 新契約申込み進捗確認一覧リストを取得する
     *
     * @param parMap key:agentNo 代理店コード、shoribi:処理日
     * @return 新契約申込み進捗確認一覧リスト
     */
    List<ContractProgressSearchResultDTO> getContractProgressList(HashMap<String, Object> parMap);

    /**
     * 支部連携の所長チェック済フラグリストを取得する
     *
     * @param shokemBango
     *            証券番号
     * @return 支部連携の所長チェック済フラグリスト
     */
    List<String> getDirCheckFlagList(String shokemBango);

    /**
     * （法人）エラーワーニングリストを取得する
     *
     * @param shokemBango
     *            証券番号
     * @return （法人）エラーワーニングリスト
     */
    List<String> getErrwarnList(String shokemBango);

    /**
     * 処理日を取得する
     *
     * @return 処理日
     */
    Date getSyoribi();

}
