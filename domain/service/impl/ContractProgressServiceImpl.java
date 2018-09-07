package jp.co.pmacmobile.domain.service.impl;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.pmacmobile.common.constant.ContractProgressConsts;
import jp.co.pmacmobile.domain.dto.ContractProgressDisplayDTO;
import jp.co.pmacmobile.domain.dto.ContractProgressSearchConditionDTO;
import jp.co.pmacmobile.domain.dto.ContractProgressSearchResultDTO;
import jp.co.pmacmobile.domain.mapper.nbq.ContractProgressMapper;
import jp.co.pmacmobile.domain.service.ContractProgressService;

/**
 * 新契約申込み進捗確認サービス実装クラス
 *
 * <ul>新契約申込み進捗確認検索機能</ul>
 *
 * @author hitachi
 *
 */
@Service
@Transactional
@Description("新契約申込み進捗確認")
public class ContractProgressServiceImpl implements ContractProgressService {

    /**
     * 新契約申込み進捗確認マップをインジェクションする
     */
    @Autowired
    ContractProgressMapper contractProgressMapper;

    /**
     * 新契約申込み進捗確認一覧リストを取得する。
     */
    @Override
    @Description("新契約申込み進捗確認情報取得処理")
    public List<ContractProgressDisplayDTO> getContractProgressList(ContractProgressSearchConditionDTO dto) {

        SimpleDateFormat sdf = new SimpleDateFormat(ContractProgressConsts.DATE_YYYYMMDD_SLASH);
        SimpleDateFormat sdfM = new SimpleDateFormat(ContractProgressConsts.DATE_YYYYMD_SLASH);
        SimpleDateFormat sdfSql = new SimpleDateFormat(ContractProgressConsts.DATE_YYYYMMDD_SQL);
        // 新契約申込み進捗確認一覧リスト
        List<ContractProgressDisplayDTO> resultList = new ArrayList<ContractProgressDisplayDTO>();
        // 処理日取得
        Date shoribi = this.contractProgressMapper.getSyoribi();
        HashMap<String, Object> parMap = new HashMap<String, Object>();
        parMap.put(ContractProgressConsts.USER_ID, dto.getUserId());
        parMap.put(ContractProgressConsts.AGENT_NO, dto.getSectCode());
        Calendar cal = Calendar.getInstance();
        cal.setTime(shoribi);
        cal.add(Calendar.DATE, -30);
        parMap.put(ContractProgressConsts.SHORIBI, sdfSql.format(cal.getTime()));
        // 新契約申込み進捗確認検索結果リスト
        List<ContractProgressSearchResultDTO> searchResultList = this.contractProgressMapper
                        .getContractProgressList(parMap);
        // 新契約申込み進捗確認検索結果DTO
        ContractProgressDisplayDTO displayDto = null;
        if (searchResultList != null && searchResultList.size() > 0) {
            // データ編集
            for (ContractProgressSearchResultDTO contractProgressSearchDto : searchResultList) {
                displayDto = new ContractProgressDisplayDTO();
                // 営業部コード
                displayDto.setAgentNo(contractProgressSearchDto.getAgentNo());
                // 証券番号
                displayDto.setSyokenbango(contractProgressSearchDto.getSyokenbango());
                // 申込日
                displayDto.setMoshikomibi(contractProgressSearchDto.getMoshikomibi() != null
                                ? sdfM.format(contractProgressSearchDto.getMoshikomibi())
                                                : ContractProgressConsts.BLANK);
                // 成立日
                displayDto.setSeiritsuKakuteibi(contractProgressSearchDto.getSeiritsuKakuteibi() != null
                                ? sdfM.format(contractProgressSearchDto.getSeiritsuKakuteibi())
                                                : ContractProgressConsts.BLANK);
                // 契約者名
                displayDto.setKeiyakushamei(contractProgressSearchDto.getKeiyakushamei());
                // 被保険者名
                displayDto.setHihokenshamei1(contractProgressSearchDto.getHihokenshamei1());
                // 募集人名
                displayDto.setBoshujinmei(contractProgressSearchDto.getShimei());
                // 代理店名
                displayDto.setDairitenmei(contractProgressSearchDto.getDairitenmei());
                // NBSR
                displayDto.setNbsrKensu(contractProgressSearchDto.getNbsrKensu());
                // ステータス
                displayDto.setStatus(getStatus(contractProgressSearchDto));
                // 全体状況
                displayDto.setZenntaijyokyo(displayDto.getStatus());
                // 体況査定
                displayDto.setTaikyosatei(getTaikyoSateiInfo(contractProgressSearchDto));

                // -------------------------------
                // 口座情報
                // -------------------------------
                // 口座番号 または 口座名義人: 対象証券番号あり
                if (!StringUtils.isBlank(contractProgressSearchDto.getKouzaBango())
                                || !StringUtils.isBlank(contractProgressSearchDto.getKouzaMeigijin())) {
                    // 反映済
                    displayDto.setKouzajyoho(ContractProgressConsts.KOUZA_STATUS_HANEIZUMI);
                    // 口座番号 または 口座名義人: 対象証券番号なし  かつ  法人エラーワーニング．エラーワーニングコード ＝ 口座情報に不備があります。
                } else {
                    // 法人エラーワーニング．エラーワーニングコード取得
                    List<String> getErrwarnList = this.contractProgressMapper
                                    .getErrwarnList(contractProgressSearchDto.getSyokenbango());
                    if (getErrwarnList != null && getErrwarnList.size() > 0) {
                        // 未着
                        displayDto.setKouzajyoho(ContractProgressConsts.TOTATSU_MICYAKU);
                    }
                }

                // -------------------------------
                // SP
                // -------------------------------
                // 申込管理．データ作成日時≧処理日
                if ((contractProgressSearchDto.getCreateDate() != null
                                ? sdf.format(contractProgressSearchDto.getCreateDate())
                                                : ContractProgressConsts.BLANK).compareTo(sdf.format(shoribi)) >= 0) {
                    displayDto.setSp(ContractProgressConsts.BLANK);
                    // 申込管理．ＳＰ査定ステータス＝"00"（初期値）の場合
                } else if (ContractProgressConsts.SPSATEI_ST_SYOKICHI
                                .equals(contractProgressSearchDto.getSpSateiStatus())) {
                    // 対象外
                    displayDto.setSp(ContractProgressConsts.SP_TAISHOGAI);
                    // 申込管理．ＳＰ査定ステータス＝"09"（完了）の場合
                } else if (ContractProgressConsts.SPSATEI_ST_KANRYO.equals(contractProgressSearchDto.getSpSateiStatus())) {
                    // 完了
                    displayDto.setSp(ContractProgressConsts.SP_KANRYO);
                    // 上記以外
                } else {
                    // SP設定
                    displayDto.setSp(setSP(contractProgressSearchDto.getSogoKekkaCD()));
                }

                // 初回入金
                displayDto.setSyokainyukin(setPay(contractProgressSearchDto.getPayStauts()));

                // -------------------------------
                // MR/SR確認
                // -------------------------------
                // 所長チェック済フラグ1
                String shochoCheckzumiFlg1 = contractProgressSearchDto.getShochoCheckzumiFlag();
                // 所長チェック済フラグ2
                String shochoCheckzumiFlg2 = contractProgressSearchDto.getShochoRanshomei1();
                // 所長チェック済フラグ3
                String shochoCheckzumiFlg3 = contractProgressSearchDto.getShochoRanshomei2();
                // 所長チェック済リスト
                List<String> shibuRenkeiShochoFlgList = this.contractProgressMapper
                                .getDirCheckFlagList(contractProgressSearchDto.getSyokenbango());
                //未了・完了確認共通処理
                boolean shochoCheck = getShochocheckALL(shibuRenkeiShochoFlgList, shochoCheckzumiFlg1,
                                shochoCheckzumiFlg2, shochoCheckzumiFlg3);
                if (shochoCheck) {
                    displayDto.setMrsrkakunin(ContractProgressConsts.SHOCHO_KAKUNIN_KANRYO);
                } else {
                    displayDto.setMrsrkakunin(ContractProgressConsts.SHOCHO_KAKUNIN_MIRYO);
                }

                resultList.add(displayDto);
            }
        } else {
            displayDto = new ContractProgressDisplayDTO();
            displayDto.setAgentNo(dto.getSectCode());
            resultList.add(displayDto);
        }

        return resultList;
    }

    /**
     * ステータス状況取得
     *
     * @param contractProgressSearchDto 新契約申込み進捗確認検索結果エンティティ
     *
     * @return ステータス状況
     */
    private String getStatus(ContractProgressSearchResultDTO contractProgressSearchDto) {
        String status = null;
        SimpleDateFormat sdf = new SimpleDateFormat(ContractProgressConsts.DATE_YYYYMMDD_SLASH);
        // 案件管理．査定結果 = 01(成立待ち)
        if (ContractProgressConsts.SATEIKEKKA_SEIRITSUMACHI.equals(contractProgressSearchDto.getSateiKekka())) {
            // 最終確認終了
            status = ContractProgressConsts.STATUSNAME_SAISHUKAKUNIN_SHURYO;
            // 案件管理．査定結果 = 02(成立)
        } else if (ContractProgressConsts.SATEIKEKKA_SEIRITSU.equals(contractProgressSearchDto.getSateiKekka())) {
            // 成立
            status = ContractProgressConsts.STATUSNAME_SEIRITSU;
            // 案件管理．査定結果 = 初期値
        } else if (StringUtils.isBlank(contractProgressSearchDto.getSateiKekka())) {
            // 不備チェックｽﾃｰﾀｽ = "09" (完了)
            if (ContractProgressConsts.FUBICHECK_ST_KANRYO.equals(contractProgressSearchDto.getFubiCheckStatus()) &&
            // １次査定ｽﾃｰﾀｽ = "09"(完了)
                            ContractProgressConsts.SATEI_ST_KANRYO
                                            .equals(contractProgressSearchDto.getIchijiSateiStatus())
                            &&
                            // ２次査定ｽﾃｰﾀｽ = "00"(初期値) || ２次査定ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.SATEI_ST_SYOKICHI
                                            .equals(contractProgressSearchDto.getNijiSateiStatus()) ||
                                            ContractProgressConsts.SATEI_ST_KANRYO
                                                            .equals(contractProgressSearchDto.getNijiSateiStatus()))
                            &&
                            // ＬＩＮＣ査定ｽﾃｰﾀｽ = "00"(初期値) || ＬＩＮＣ査定ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.LNCASS_ST_LINC_KANRYO
                                            .equals(contractProgressSearchDto.getLincSateiStatus()) ||
                                            ContractProgressConsts.LNCASS_ST_LINC_SYOKICHI
                                                            .equals(contractProgressSearchDto.getLincSateiStatus()))
                            &&
                            // 医事照会ｽﾃｰﾀｽ = "00"(初期値) || 医事照会ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.IJISHOKAI_ST_KANRYO
                                            .equals(contractProgressSearchDto.getIjiShokaiStatus()) ||
                                            ContractProgressConsts.IJISHOKAI_ST_SYOKICHI
                                                            .equals(contractProgressSearchDto.getIjiShokaiStatus()))
                            &&
                            // 社医査定ｽﾃｰﾀｽ = "00"(初期値) || 社医査定ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.SHAISATEI_ST_KANRYO
                                            .equals(contractProgressSearchDto.getShaiSateiStatus()) ||
                                            ContractProgressConsts.SHAISATEI_ST_SYOKICHI
                                                            .equals(contractProgressSearchDto.getShaiSateiStatus()))
                            &&
                            // 体況査定＿支部開示日時 ≠ 初期値
                            !ContractProgressConsts.DEFAULT_DATE
                                            .equals(contractProgressSearchDto.getTaikyoSateiShibuKaijiNichiji() != null
                                                            ? sdf.format(contractProgressSearchDto
                                                                            .getTaikyoSateiShibuKaijiNichiji())
                                                            : ContractProgressConsts.BLANK)
                            &&
                            // 体況査定結果 = 初期値 || 体況査定結果 = "11"
                            ((ContractProgressConsts.TAIKYOSATEIKEKKA_ST_SYOKICHI
                                            .equals(contractProgressSearchDto.getTaikyoSateiKekka()) ||
                                            ContractProgressConsts.TAIKYOSATEIKEKKA_ST_11
                                                            .equals(contractProgressSearchDto.getTaikyoSateiKekka()))
                                            ||
                                            // 特別条件通知．支部表示確定日 ≠ 初期値
                                            !ContractProgressConsts.DEFAULT_DATE.equals(
                                                            contractProgressSearchDto.getShibuHyojiKakuteibi() != null
                                                                            ? sdf.format(contractProgressSearchDto
                                                                                            .getShibuHyojiKakuteibi())
                                                                            : ContractProgressConsts.BLANK))
                            &&
                            // ＳＰ査定ｽﾃｰﾀｽ = "00"(初期値) || ＳＰ査定ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.SPSATEI_ST_KANRYO
                                            .equals(contractProgressSearchDto.getSpSateiStatus()) ||
                                            ContractProgressConsts.SPSATEI_ST_SYOKICHI
                                                            .equals(contractProgressSearchDto.getSpSateiStatus()))
                            &&
                            // 法人査定ｽﾃｰﾀｽ = "00"(初期値) || 法人査定ｽﾃｰﾀｽ = "09"(完了)
                            (ContractProgressConsts.HOUJINSATEI_ST_KANRYO
                                            .equals(contractProgressSearchDto.getHojinSateiStatus()) ||
                                            ContractProgressConsts.HOUJINSATEI_ST_SYOKICHI
                                                            .equals(contractProgressSearchDto.getHojinSateiStatus()))) {
                // 最終確認
                status = ContractProgressConsts.STATUSNAME_SAISHUKAKUNIN;
            } else {
                // 査定中
                status = ContractProgressConsts.STATUSNAME_SATEITHU;
            }
            // 案件管理．査定結果 = "03"(不成立待ち) || 案件管理．査定結果 = "05"(ｸｰﾘﾝｸﾞｵﾌ待ち) || 案件管理．査定結果 = "07"(謝絶待ち)
        } else if (ContractProgressConsts.SATEIKEKKA_FUSEIRITSUMACHI.equals(contractProgressSearchDto.getSateiKekka())
                        ||
                        ContractProgressConsts.SATEIKEKKA_COOLINGOFFMACHI
                                        .equals(contractProgressSearchDto.getSateiKekka())
                        ||
                        ContractProgressConsts.SATEIKEKKA_SHAZETSUMACHI
                                        .equals(contractProgressSearchDto.getSateiKekka())) {
            // 査定中
            status = ContractProgressConsts.STATUSNAME_SATEITHU;
            // 案件管理．査定結果 = "04"(不成立) || 案件管理．査定結果 = "06"(ｸｰﾘﾝｸﾞｵﾌ) || 案件管理．査定結果 = "08"(謝絶)
        } else if (ContractProgressConsts.SATEIKEKKA_FUSEIRITSU.equals(contractProgressSearchDto.getSateiKekka()) ||
                        ContractProgressConsts.SATEIKEKKA_COOLINGOFF.equals(contractProgressSearchDto.getSateiKekka())
                        ||
                        ContractProgressConsts.SATEIKEKKA_SHAZETSU.equals(contractProgressSearchDto.getSateiKekka())) {
            // 不成立
            status = ContractProgressConsts.STATUSNAME_FUSEIRITSU;
        }
        return status;
    }

    /**
     * 体況査定結果を元に体況査定情報を返す。
     *
     * @return 体況査定情報
     */
    private String getTaikyoSateiInfo(ContractProgressSearchResultDTO contractProgressSearchDto) {

        String taikyouStatus = null;
        SimpleDateFormat sdf = new SimpleDateFormat(ContractProgressConsts.DATE_YYYYMMDD_SLASH);
        // 案件管理．再保険ステータス＝"02"(照会中)の場合
        if (StringUtils.equals(ContractProgressConsts.SAIHOKENSTATUS_SHOKAICHU,
                        contractProgressSearchDto.getSaihokennStatus())) {
            // 再保照会中
            taikyouStatus = ContractProgressConsts.STATUSNAME_SAIHOSHOKAICHU;
            // 案件管理．人為査定工程＝"14"(医事照会)の場合
        } else if (StringUtils.equals(ContractProgressConsts.HASSPR_IJISHOKAI, contractProgressSearchDto.getHasspr())) {
            // 医事照会中
            taikyouStatus = ContractProgressConsts.STATUSNAME_IJISHOKAICHU;
        } else {
            // 体況査定＿支部開示日時が"1900/01/01"の場合
            if (ContractProgressConsts.DEFAULT_DATE
                            .equals(contractProgressSearchDto.getTaikyoSateiShibuKaijiNichiji() != null
                                            ? sdf.format(contractProgressSearchDto.getTaikyoSateiShibuKaijiNichiji())
                                            : contractProgressSearchDto.getTaikyoSateiShibuKaijiNichiji())) {
                // 空文字
                taikyouStatus = ContractProgressConsts.BLANK;
            } else {
                // 体況査定＿支部開示日時が"1900/01/01"以外の場合
                // 体況査定結果
                String sateiKekka = contractProgressSearchDto.getTaikyoSateiKekka();
                // 体況査定結果 = "11"（無条件、引受可、成立（無条件）)の場合
                if (ContractProgressConsts.TAIKYO_SASTEI_KEKKA_MUJYOKEN.equals(sateiKekka)) {
                    taikyouStatus = ContractProgressConsts.TAIKYO_SASTEI_MUJYOKEN;
                    // 体況査定結果 = "12"（特別条件、条件付引受、成立（特別条件））かつ
                    // 支部表示確定日が"1900/01/01"でない場合
                } else if (ContractProgressConsts.TAIKYO_SASTEI_KEKKA_TOKUBETSU.equals(sateiKekka)
                                && !ContractProgressConsts.DEFAULT_DATE
                                                .equals(contractProgressSearchDto.getShibuHyojiKakuteibi() != null
                                                                ? sdf.format(contractProgressSearchDto
                                                                                .getShibuHyojiKakuteibi())
                                                                : contractProgressSearchDto.getShibuHyojiKakuteibi())) {
                    taikyouStatus = ContractProgressConsts.TAIKYO_SASTEI_TOKUBETSU;
                    /*
                     * 体況査定結果が上１桁＝"R"（謝絶扱い）の場合
                     *   かつ
                     * 支部表示確定日が"1900/01/01"でない場合
                     */
                } else if (ContractProgressConsts.TAIKYO_SASTEI_KEKKA_SYAZETSUATSUKAI
                                .equals(StringUtils.substring(sateiKekka, 0, 1))
                                && !ContractProgressConsts.DEFAULT_DATE
                                                .equals(contractProgressSearchDto.getShibuHyojiKakuteibi() != null
                                                                ? sdf.format(contractProgressSearchDto
                                                                                .getShibuHyojiKakuteibi())
                                                                : contractProgressSearchDto.getShibuHyojiKakuteibi())) {
                    taikyouStatus = ContractProgressConsts.TAIKYO_SASTEI_SYAZETSU;
                    // 上記以外
                } else {
                    taikyouStatus = ContractProgressConsts.BLANK;
                }
            }
        }

        return taikyouStatus;
    }

    /**
     * ＳＰ管理・ＳＰ管理（参照用）．総合結果コードの値を変換
     *
     * @param arg 総合結果コード
     *
     * @return SP表示内容
     */
    private String setSP(String arg) {
        String sp = null;
        if (arg != null) {
            switch (arg) {
            // 総合結果コード = "00" 未了
            case ContractProgressConsts.SOGOKEKKA_CD_00:
                sp = ContractProgressConsts.SP_MIRYO;
                break;
            // 総合結果コード = "01" 完了
            case ContractProgressConsts.SOGOKEKKA_CD_01:
                sp = ContractProgressConsts.SP_KANRYO;
                break;
            // 総合結果コード = "02" 再確認中
            case ContractProgressConsts.SOGOKEKKA_CD_02:
                sp = ContractProgressConsts.SP_SAIKAKUNINCHU;
                break;
            // 総合結果コード = "03" 確認項目不足
            case ContractProgressConsts.SOGOKEKKA_CD_03:
                sp = ContractProgressConsts.SP_KAKUNINKOMOKUFUSOKU;
                break;
            // 総合結果コード = "04" 電話不通
            case ContractProgressConsts.SOGOKEKKA_CD_04:
                sp = ContractProgressConsts.SP_DENWAFUTU;
                break;
            // 総合結果コード = "05" 対象外
            case ContractProgressConsts.SOGOKEKKA_CD_05:
                sp = ContractProgressConsts.SP_TAISHOGAI;
                break;
            // 総合結果コード = "06" 再確認中（リトライ）
            case ContractProgressConsts.SOGOKEKKA_CD_06:
                sp = ContractProgressConsts.SP_RETRY;
                break;
            // 総合結果コード = "07" 再確認中（引受不可）
            case ContractProgressConsts.SOGOKEKKA_CD_07:
                sp = ContractProgressConsts.SP_HIKIUKEFUKA;
                break;
            // 総合結果コード = "08" 一時不通
            case ContractProgressConsts.SOGOKEKKA_CD_08:
                sp = ContractProgressConsts.SP_ICHIJIFUTU;
                break;
            // 総合結果コード = "09" 中止
            case ContractProgressConsts.SOGOKEKKA_CD_09:
                sp = ContractProgressConsts.SP_CHUSHI;
                break;
            // 総合結果コード = "11" 保障・適合性ＮＧ
            case ContractProgressConsts.SOGOKEKKA_CD_11:
                sp = ContractProgressConsts.SP_HOSHO_TEKIGOSEI_NG;
                break;
            default:
                sp = ContractProgressConsts.BLANK;
            }
        }
        return sp;
    }

    /**
     * 入金ステータスの値を変換
     *
     * @param payStauts 入金ステータス
     *
     * @return 初回入金表示内容
     */
    private String setPay(String payStauts) {
        String pay = null;
        switch (payStauts) {
        // 入金ステータス = "00" 未入金
        case ContractProgressConsts.NYUKIN_STATUS_CD_00:
            pay = ContractProgressConsts.NYUKIN_STATUS_MINYUKIN;
            break;
        // 入金ステータス = "01" 仮申込
        case ContractProgressConsts.NYUKIN_STATUS_CD_01:
            pay = ContractProgressConsts.BLANK;
            break;
        // 入金ステータス = "02" 入金前査定
        case ContractProgressConsts.NYUKIN_STATUS_CD_02:
            pay = ContractProgressConsts.BLANK;
            break;
        // 入金ステータス = "03" 入金済み
        case ContractProgressConsts.NYUKIN_STATUS_CD_03:
            pay = ContractProgressConsts.NYUKIN_STATUS_NYUKINZUMI;
            break;
        // 入金ステータス = "04" 不足
        case ContractProgressConsts.NYUKIN_STATUS_CD_04:
            pay = ContractProgressConsts.NYUKIN_STATUS_FUSOKU;
            break;
        // 入金ステータス = "05" 過金
        case ContractProgressConsts.NYUKIN_STATUS_CD_05:
            pay = ContractProgressConsts.NYUKIN_STATUS_KAKIN;
            break;
        // 入金ステータス = "06" 初回源泉（口座）
        case ContractProgressConsts.NYUKIN_STATUS_CD_06:
            pay = ContractProgressConsts.NYUKIN_STATUS_SHOKAI_KOUZA;
            break;
        // 入金ステータス = "07" 初回源泉（団体）
        case ContractProgressConsts.NYUKIN_STATUS_CD_07:
            pay = ContractProgressConsts.NYUKIN_STATUS_SHOKAI_DANTAI;
            break;
        default:
            pay = ContractProgressConsts.BLANK;
        }
        return pay;
    }

    /**
     * 所長チェック済かどうかをチェックする。
     *
     * @param list 支部連携.所長チェック済フラグ
     * @param torihou 取扱報告書の所長チェック済フラグ
     * @param iko 意向確認書の所長チェック済フラグ
     * @param jusetsu 重要事項確認書の所長チェック済フラグ
     *
     * @return false:所長チェック未済 / true:所長チェック済
     */
    private boolean getShochocheckALL(List<String> list, String torihou, String iko, String jusetsu) {

        // 各本テーブルの所長チェック済フラグがブランクだった場合、0に変換する
        if (ContractProgressConsts.BLANK.equals(torihou)) {
            torihou = ContractProgressConsts.SHOCHO_CHECK_MIRYO;
        }
        if (ContractProgressConsts.BLANK.equals(iko)) {
            iko = ContractProgressConsts.SHOCHO_CHECK_MIRYO;
        }
        if (ContractProgressConsts.BLANK.equals(jusetsu)) {
            jusetsu = ContractProgressConsts.SHOCHO_CHECK_MIRYO;
        }

        // すべて未スキャンの場合、falseを返す
        if (torihou == null && iko == null && jusetsu == null) {
            return false;
            //取報・意向・重説の中に、一つでも所長チェック未了のものがあれば、falseを返す
        } else if ((ContractProgressConsts.SHOCHO_CHECK_MIRYO.equals(torihou)
                        && !list.contains(ContractProgressConsts.SHOCHO_CHECK_KANRYO1))
                        || (ContractProgressConsts.SHOCHO_CHECK_MIRYO.equals(iko)
                                        && !list.contains(ContractProgressConsts.SHOCHO_CHECK_KANRYO2))
                        || (ContractProgressConsts.SHOCHO_CHECK_MIRYO.equals(jusetsu)
                                        && !list.contains(ContractProgressConsts.SHOCHO_CHECK_KANRYO3))) {
            return false;
            //上記以外
        } else {
            return true;
        }
    }

}
