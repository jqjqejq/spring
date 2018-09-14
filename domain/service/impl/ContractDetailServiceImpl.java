package jp.co.pmacmobile.domain.service.impl;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.pmacmobile.common.constant.ContractDetailConsts;
import jp.co.pmacmobile.common.constant.MessageCode;
import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.domain.dto.ContractDetailAnkenkanriInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailAnswerDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailAnswerParameterDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailChohyokanriInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailCwaParameterDto;
import jp.co.pmacmobile.domain.dto.ContractDetailDairitenInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailDairitenjohoShosaiDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHojinAnkenHojinMoshikomiInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHojinAnkenkanriInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHojinNbsrShibukaijitaijokakuninDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHojoInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHoshonaiyoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailHoshonaiyoShosaiDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailKeiyakunaiyoShosaiDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailKokuchishoInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailMoshikomiAnkentaishoInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailMoshikomishoInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailNbsrDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailNbsrFukaitokanoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailNbsrInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailNbsrKaitokanoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailNbsrShibukaijitaijokakuninDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailResultDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailRiderInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailSateijokyoShosaiDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailSearchConditionDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailShiburenkeiInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailTSSNyukinInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailTSubviewDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailToriatsukaishaDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailTsukaMasterInfoDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailTsushinsakiDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailUketorininDTO;
import jp.co.pmacmobile.domain.dto.ContractDetailUketorininInfoDTO;
import jp.co.pmacmobile.domain.mapper.nbq.ContractDetailMapper;
import jp.co.pmacmobile.domain.service.ContractDetailService;

/**
 * 詳細を見るサービス実装クラス
 *
 * <ul>詳細情報取得機能</ul>
 * <ul>NBSR回答登録機能</ul>
 *
 */
@Service
@Transactional
@Description("詳細を見る")
public class ContractDetailServiceImpl implements ContractDetailService {

    /**
     * 詳細を見るマップをインジェクションする
     */
    @Autowired
    ContractDetailMapper contractDetailMapper;

    /**
     * メッセージソースをインジェクションする
     */
    @Autowired
    MessageSource messageSource;

    /**
     * 詳細情報を取得する。
     *
     * @param searchConditionDto 検索条件DTO
     * @return 詳細内容
     * @throws Exception 例外
     */
    @Override
    @Description("詳細情報取得処理")
    public ContractDetailResultDTO getDetail(ContractDetailSearchConditionDTO searchConditionDto)
                    throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMD_SLASH);
        SimpleDateFormat sdfs = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMMDD_SLASH);
        // 返却結果DTO
        ContractDetailResultDTO resultDto = null;
        // 検索条件マップ
        HashMap<String, Object> objParMap = null;
        // 検索条件リスト
        List<String> parList = null;
        // 対象証券番号取得
        String shokenbango = searchConditionDto.getSyokenbango();

        // (1) 申込書ビューを取得する
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.USER_ID, searchConditionDto.getUserID());
        ContractDetailTSubviewDTO tSubviewInfo = this.contractDetailMapper.getMoshikomishoView(objParMap);

        // (2) チェック処理を行う。
        if (tSubviewInfo == null) {
            throw new MobileException(MessageCode.MSG1005W,
                            this.messageSource.getMessage(MessageCode.MSG1005W, null, null));
        }
        // 入金情報取得条件Dto
        ContractDetailCwaParameterDto cwaParameterDto = new ContractDetailCwaParameterDto();
        cwaParameterDto.setShokemBango(shokenbango);
        cwaParameterDto.setHaraikataHohoKeiroCD(tSubviewInfo.getHaraikataHohoKeiroCD());
        // (3) 入金情報、ＴＳＳレートを取得する
        ContractDetailTSSNyukinInfoDTO tssNyukinInfo = this.contractDetailMapper.getTSSInfoNyukinInfo(cwaParameterDto);

        // (5) 申込書情報を取得する
        // 申込書情報取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.EDABAN, tSubviewInfo.getEdaban());
        // 検索を実行する
        ContractDetailMoshikomishoInfoDTO moshikomishoInfo = this.contractDetailMapper.getMoshikomishoInfo(objParMap);

        // (6) 同時契約証券番号を取得する
        List<ContractDetailMoshikomiAnkentaishoInfoDTO> moshikomiAnkentaishoInfoList = this.contractDetailMapper
                        .getMoshikomiAnkentaishoInfo(shokenbango);

        // (7) 告知書情報を取得する
        ContractDetailKokuchishoInfoDTO kokuchishoInfo = null;
        if (moshikomiAnkentaishoInfoList != null && moshikomiAnkentaishoInfoList.size() != 0) {
            // 告知書情報取得条件マップ
            objParMap = new HashMap<String, Object>();
            // 告知書情報取得条件証券番号リスト
            parList = new ArrayList<String>();
            for (ContractDetailMoshikomiAnkentaishoInfoDTO contractDetailMoshikomiAnkentaishoInfoDTO : moshikomiAnkentaishoInfoList) {
                parList.add(contractDetailMoshikomiAnkentaishoInfoDTO.getShokemBango());
            }
            // マップに証券番号リストを設定する
            objParMap.put(ContractDetailConsts.SHOKEM_BANGO_ARRAY, parList);
            // 告知書情報取得条件帳票IDリスト
            parList = new ArrayList<String>();
            parList.add(ContractDetailConsts.DOCUMENT_ID_04001);
            parList.add(ContractDetailConsts.DOCUMENT_ID_04006);
            // マップに帳票IDリストを設定する
            objParMap.put(ContractDetailConsts.CHOHYO_ID, parList);
            // 検索を実行する
            kokuchishoInfo = this.contractDetailMapper.getKokuchishoInfo(objParMap);
        }

        // (8) 受取人情報を取得する
        // 受取人情報・ライダー情報取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.EDABAN, tSubviewInfo.getEdaban());
        // 検索を実行する
        List<ContractDetailUketorininInfoDTO> uketorininInfoList = this.contractDetailMapper
                        .getUketorininInfo(objParMap);
        // (9) ライダー情報を取得する
        List<ContractDetailRiderInfoDTO> riderInfoList = this.contractDetailMapper.getRiderInfo(objParMap);

        // -------------------------------------------
        // (10) ＮＢＳＲ欄情報を取得する
        // -------------------------------------------
        // <I> 代表証券番号を取得する
        String daihyoShokenBango = this.contractDetailMapper.getHojinMoshikomiKanri(shokenbango);

        // <II> ＮＢＳＲ情報取得
        // ＮＢＳＲ情報取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.DAIHYO_SHOKEM_BANGO, daihyoShokenBango);
        // 検索を実行する
        List<ContractDetailNbsrInfoDTO> nbsrInfoList = this.contractDetailMapper.getNbsrInfo(objParMap);
        for (ContractDetailNbsrInfoDTO contractDetailNbsrInfoDTO : nbsrInfoList) {
            // <III> 支部表示対象の情報を取得する
            // 支部表示対象の情報取得条件マップ
            objParMap = new HashMap<String, Object>();
            objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
            objParMap.put(ContractDetailConsts.ERROR_WARNING_REMBAN, contractDetailNbsrInfoDTO.getErrwarNo());
            objParMap.put(ContractDetailConsts.NBSR_REMBAN, contractDetailNbsrInfoDTO.getNbsNo());
            objParMap.put(ContractDetailConsts.SHUTOKUMOTO_KUBUN, contractDetailNbsrInfoDTO.getNbsrKbn());
            objParMap.put(ContractDetailConsts.HOKOKUBI, contractDetailNbsrInfoDTO.getRepDt());
            // 検索を実行する
            ContractDetailShiburenkeiInfoDTO shiburenkeiInfo = this.contractDetailMapper.getShiburenkeiInfo(objParMap);
            if (shiburenkeiInfo != null) {
                // 回答日
                contractDetailNbsrInfoDTO.setAnsDt(shiburenkeiInfo.getKaitobi());
                // 回答内容
                contractDetailNbsrInfoDTO.setAnsCnt(shiburenkeiInfo.getKaitoNaiyo());
            }
        }

        // -------------------------------------------
        // (11) 取扱報告書（個人用）表示内容取得
        // -------------------------------------------
        // <I> 個人用保険取扱報告書情報取得
        String shochoCheckzumiFlag = this.contractDetailMapper.getTorihoHyojiInfo(shokenbango);

        // (12) 取扱報告書（事業保険用）タブ表示内容取得
        String shochoCheckFlag = this.contractDetailMapper.getJigyotorihoHyojiInfo(shokenbango);

        // (13) 代理店情報表示内容取得
        // 代理店情報１表示内容取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.TORIATUKAI_FLAG, ContractDetailConsts.DAIRITEN_INFO_1);
        // 代理店情報１検索を実行する
        ContractDetailDairitenInfoDTO dairitenInfo1 = this.contractDetailMapper.getDairitenInfo(objParMap);
        // 代理店情報２表示内容取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.TORIATUKAI_FLAG, ContractDetailConsts.DAIRITEN_INFO_2);
        // 代理店情報１検索を実行する
        ContractDetailDairitenInfoDTO dairitenInfo2 = this.contractDetailMapper.getDairitenInfo(objParMap);

        // -------------------------------------------
        // (14) 告知日情報を取得する
        // -------------------------------------------
        // 告知日
        String kokuchiDt = ContractDetailConsts.BLANK;
        List<ContractDetailChohyokanriInfoDTO> chohyokanriInfoList = null;
        // <III> 機械作成フラグを取得する
        String getKikaisakuseiFlag = this.contractDetailMapper.getKikaisakuseiFlag(moshikomishoInfo.getChohyoID());
        if (ContractDetailConsts.HOKENTYPE_CD_11.equals(moshikomishoInfo.getShukeiyakuHokenshuruiCD())
                        && !ContractDetailConsts.DOCUMENT_TYPE_KIKAI_SAKUSEI.equals(getKikaisakuseiFlag)) {
            kokuchiDt = sdf.format(tSubviewInfo.getMoshikomibi());
        } else if (ContractDetailConsts.IMU_CD_0.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_5.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_6.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_7.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_B.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_3.equals(moshikomishoInfo.getImuCD1())) {
            if (kokuchishoInfo != null) {
                kokuchiDt = sdf.format(kokuchishoInfo.getKokuchibi());
            }
        } else if (ContractDetailConsts.IMU_CD_1.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_2.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_9.equals(moshikomishoInfo.getImuCD1())) {
            objParMap.put(ContractDetailConsts.CHOHYO_ID, ContractDetailConsts.DOCUMENT_ID_05002);
            // 検索を実行する
            chohyokanriInfoList = this.contractDetailMapper.getChohyokanriInfo(objParMap);
            if (chohyokanriInfoList != null && chohyokanriInfoList.size() > 0) {
                // 報状情報取得条件マップ
                objParMap = new HashMap<String, Object>();
                objParMap.put(ContractDetailConsts.CHOHYO_ID, chohyokanriInfoList.get(0).getChohyoID());
                objParMap.put(ContractDetailConsts.SERIAL_BANGO, chohyokanriInfoList.get(0).getSerialBango());
                // 検索を実行する
                List<ContractDetailHojoInfoDTO> hojoInfoList = this.contractDetailMapper.getHojoInfo(objParMap);
                if (hojoInfoList.size() > 0) {
                    kokuchiDt = sdf.format(hojoInfoList.get(0).getExmDt());
                }
            }
        } else if (ContractDetailConsts.IMU_CD_4.equals(moshikomishoInfo.getImuCD1())
                        || ContractDetailConsts.IMU_CD_A.equals(moshikomishoInfo.getImuCD1())) {
            objParMap.put(ContractDetailConsts.CHOHYO_ID, ContractDetailConsts.DOCUMENT_ID_05006);
            // 検索を実行する
            chohyokanriInfoList = this.contractDetailMapper.getChohyokanriInfo(objParMap);
            if (chohyokanriInfoList != null && chohyokanriInfoList.size() > 0) {
                // 報状情報取得条件マップ
                objParMap = new HashMap<String, Object>();
                objParMap.put(ContractDetailConsts.CHOHYO_ID, chohyokanriInfoList.get(0).getChohyoID());
                objParMap.put(ContractDetailConsts.SERIAL_BANGO, chohyokanriInfoList.get(0).getSerialBango());
                // 検索を実行する
                List<ContractDetailHojoInfoDTO> hojoInfoList = this.contractDetailMapper.getHojoInfo(objParMap);
                if (hojoInfoList.size() > 0) {
                    kokuchiDt = sdf.format(hojoInfoList.get(0).getIvwDt());
                }
            }
        }

        // -------------------------------------------
        // (15) 申込書類到着状況情報を取得する
        // -------------------------------------------
        // <I> a) 意向確認書（提出必要書類）
        // 意向確認書（提出必要書類）取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.DAIHYO_SHOKEM_BANGO, daihyoShokenBango);
        // 所長欄署名検索を実行する
        String dbrFg = this.contractDetailMapper.getIkokakuninshoInfo(objParMap);

        // <I> b) 重要事項確認書（提出必要書類）
        String juyoIkokakuninshoInfo = this.contractDetailMapper.getJuyoIkokakuninshoInfo(shokenbango);

        // <I> c) 団体加入確認
        // 団体加入確認取得条件マップ
        objParMap = new HashMap<String, Object>();
        // 団体加入確認取得条件帳票IDリスト
        parList = new ArrayList<String>();
        parList.add(ContractDetailConsts.DOCUMENT_ID_02028);
        parList.add(ContractDetailConsts.DOCUMENT_ID_02027038);
        objParMap.put(ContractDetailConsts.CHOHYO_ID, parList);
        // 団体加入確認取得条件証券番号リスト
        parList = new ArrayList<String>();
        parList.add(shokenbango);
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO_ARRAY, parList);
        // 検索を実行する
        String dantaikakuninInfo = this.contractDetailMapper.getDantaikakuninInfo(objParMap);

        // <II> a) エラーワーニングテーブルの取得
        // エラーワーニングテーブルの取得条件マップ
        objParMap = new HashMap<String, Object>();
        // エラーワーニングテーブルの取得条件エラーワーニングリスト
        parList = new ArrayList<String>();
        parList.add(ContractDetailConsts.ERROR_ID_06143);
        parList.add(ContractDetailConsts.ERROR_ID_00143);
        parList.add(ContractDetailConsts.ERROR_ID_03143);
        parList.add(ContractDetailConsts.ERROR_ID_17097);
        parList.add(ContractDetailConsts.ERROR_ID_17001);
        parList.add(ContractDetailConsts.ERROR_ID_17115);
        objParMap.put(ContractDetailConsts.ERROR_WARNING, parList);
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.DAIHYO_SHOKEM_BANGO, daihyoShokenBango);
        // 検索を実行する
        List<String> errwarInfo = this.contractDetailMapper.getErrwarInfo(objParMap);

        // <II> b) 法人を含むエラーワーニングテーブルの取得
        // 法人エラーワーニングテーブルの取得条件マップ
        objParMap = new HashMap<String, Object>();
        // 法人エラーワーニングテーブルの取得条件法人エラーワーニングリスト
        parList = new ArrayList<String>();
        parList.add(ContractDetailConsts.ERROR_ID_06143);
        parList.add(ContractDetailConsts.ERROR_ID_00143);
        parList.add(ContractDetailConsts.ERROR_ID_03143);
        parList.add(ContractDetailConsts.ERROR_ID_17097);
        parList.add(ContractDetailConsts.ERROR_ID_17001);
        parList.add(ContractDetailConsts.ERROR_ID_17115);
        objParMap.put(ContractDetailConsts.ERROR_WARNING, parList);
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        objParMap.put(ContractDetailConsts.DAIHYO_SHOKEM_BANGO, daihyoShokenBango);
        List<String> hojinErrwarInfo = this.contractDetailMapper.getHojinErrwarInfo(objParMap);

        // -------------------------------------------
        // (16) 進捗状況を取得する
        // -------------------------------------------
        // <I> a) 支部表示確定日を取得する
        Date shibuhyojiKakuteibi = this.contractDetailMapper.getShibuhyojiKakuteibi(shokenbango);

        // <I> b) 案件管理テーブルを取得する
        List<ContractDetailAnkenkanriInfoDTO> ankenkanriInfoList = new ArrayList<ContractDetailAnkenkanriInfoDTO>();
        List<ContractDetailHojinNbsrShibukaijitaijokakuninDTO> hojinNbsrShibukaijitaijokakuninList = new ArrayList<ContractDetailHojinNbsrShibukaijitaijokakuninDTO>();
        List<ContractDetailNbsrShibukaijitaijokakuninDTO> nbsrShibukaijitaijokakuninList = new ArrayList<ContractDetailNbsrShibukaijitaijokakuninDTO>();
        List<String> shiburenkeiInfoList = new ArrayList<String>();
        if (moshikomiAnkentaishoInfoList != null && moshikomiAnkentaishoInfoList.size() > 0) {
            ankenkanriInfoList = this.contractDetailMapper
                            .getAnkenkanriInfo(moshikomiAnkentaishoInfoList.get(0).getAnkenkanriBango());
            // ＮＢＳＲテーブルが支部開示対象か確認検索条件リスト
            parList = new ArrayList<String>();
            for (ContractDetailMoshikomiAnkentaishoInfoDTO moshikomiAnkentaishoInfoDTO : moshikomiAnkentaishoInfoList) {
                parList.add(moshikomiAnkentaishoInfoDTO.getShokemBango());
            }
            // ＮＢＳＲテーブルが支部開示対象か確認条件マップ
            objParMap = new HashMap<String, Object>();
            objParMap.put(ContractDetailConsts.SHOKEM_BANGO, parList);
            // <I> c) i) b)にて取得した案件管理テーブルより、ＮＢＳＲテーブルが支部開示対象か確認する
            nbsrShibukaijitaijokakuninList = this.contractDetailMapper
                            .getNbsrShibukaijitaijokakunin(objParMap);

            // <I> c) ii) 支部連携情報を取得する
            for (ContractDetailNbsrShibukaijitaijokakuninDTO contractDetailNbsrShibukaijitaijokakuninDTO : nbsrShibukaijitaijokakuninList) {
                // 支部表示対象の情報取得条件マップ
                objParMap = new HashMap<String, Object>();
                objParMap.put(ContractDetailConsts.SHOKEM_BANGO,
                                contractDetailNbsrShibukaijitaijokakuninDTO.getShokemBango());
                objParMap.put(ContractDetailConsts.ERROR_WARNING_REMBAN,
                                contractDetailNbsrShibukaijitaijokakuninDTO.getErrorWarningRemban());
                objParMap.put(ContractDetailConsts.NBSR_REMBAN,
                                contractDetailNbsrShibukaijitaijokakuninDTO.getNbsrRemban());
                objParMap.put(ContractDetailConsts.SHUTOKUMOTO_KUBUN, ContractDetailConsts.TORIHIKI_INFO_KBN1);
                // 検索を実行する
                ContractDetailShiburenkeiInfoDTO shiburenkeiinfo1 = this.contractDetailMapper
                                .getShiburenkeiInfo(objParMap);
                if (shiburenkeiinfo1 != null) {
                    shiburenkeiInfoList.add(ContractDetailConsts.KAITOU_STATUS_1);
                } else {
                    shiburenkeiInfoList.add(ContractDetailConsts.KAITOU_STATUS_0);
                }
            }
            // <I> c) iii) 法人ＮＢＳＲテーブルが支部開示対象か確認する
            hojinNbsrShibukaijitaijokakuninList = this.contractDetailMapper
                            .getHojinNbsrShibukaijitaijokakunin(daihyoShokenBango);
            // <I> c) iv) iii)で取得した件数ごとに、支部連携情報を取得する
            for (ContractDetailHojinNbsrShibukaijitaijokakuninDTO hojinNbsrShibukaijitaijokakuninDTO : hojinNbsrShibukaijitaijokakuninList) {
                // 支部表示対象の情報取得条件マップ
                objParMap = new HashMap<String, Object>();
                objParMap.put(ContractDetailConsts.SHOKEM_BANGO,
                                hojinNbsrShibukaijitaijokakuninDTO.getDaihyoShokemBango());
                objParMap.put(ContractDetailConsts.ERROR_WARNING_REMBAN,
                                hojinNbsrShibukaijitaijokakuninDTO.getErrorWarningRemban());
                objParMap.put(ContractDetailConsts.NBSR_REMBAN, hojinNbsrShibukaijitaijokakuninDTO.getNbsrRemban());
                objParMap.put(ContractDetailConsts.SHUTOKUMOTO_KUBUN, ContractDetailConsts.TORIHIKI_INFO_KBN2);
                objParMap.put(ContractDetailConsts.HOKOKUBI, hojinNbsrShibukaijitaijokakuninDTO.getHokokubi());
                // 検索を実行する
                ContractDetailShiburenkeiInfoDTO shiburenkeiinfo2 = this.contractDetailMapper
                                .getShiburenkeiInfo(objParMap);
                if (shiburenkeiinfo2 != null) {
                    shiburenkeiInfoList.add(ContractDetailConsts.KAITOU_STATUS_1);
                } else {
                    shiburenkeiInfoList.add(ContractDetailConsts.KAITOU_STATUS_0);
                }
            }
        }

        // <I> d) 不成立延期願いの提出有無を取得する
        // 支不成立延期願いの提出有無取得条件帳票IDリスト
        parList = new ArrayList<String>();
        parList.add(ContractDetailConsts.DOCUMENT_ID_08003);
        parList.add(ContractDetailConsts.DOCUMENT_ID_08004);
        // 支不成立延期願いの提出有無取得条件マップ
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.CHOHYO_ID, parList);
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokenbango);
        List<String> fuseritsuEnkiInfoList = this.contractDetailMapper.getFuseritsuEnkiInfo(objParMap);

        // <I> e) 初回入金日情報を取得する
        Date shokainiukinbiInfo = this.contractDetailMapper.getShokainiukinbiInfo(shokenbango);

        // (17) 支部連携所長チェック取得
        List<String> shibuShochoCheckList = this.contractDetailMapper.getShibuShochoCheckInfo(shokenbango);

        // -------------------------------------------
        // (19) 法人査定ステータスを取得する
        // -------------------------------------------
        // <I> 法人・申込対照表情報を取得する
        String hojinMoshikomiTaishoInfo = this.contractDetailMapper.getHojinMoshikomiTaishoInfo(shokenbango);

        // <II> 法人案件・法人申込対照表情報を取得する
        ContractDetailHojinAnkenHojinMoshikomiInfoDTO hojinAnkenHojinMoshikomiInfo = this.contractDetailMapper
                        .getHojinAnkenHojinMoshikomiInfo(hojinMoshikomiTaishoInfo);

        // <III> 法人案件管理情報を取得する
        ContractDetailHojinAnkenkanriInfoDTO hojinAnkenkanriInfo = this.contractDetailMapper
                        .getHojinAnkenKanriInfo(hojinAnkenHojinMoshikomiInfo);

        // -------------------------------------------
        // DBから取得したデータを編集する
        // -------------------------------------------
        // 返却結果DTO
        resultDto = new ContractDetailResultDTO();

        // -------------------------------------------
        // NBSR回答可能/不可能
        // -------------------------------------------
        // NBSR回答可能/不可能DTO
        ContractDetailNbsrDTO nbsr = new ContractDetailNbsrDTO();
        // NBSR回答可能DTO
        ContractDetailNbsrKaitokanoDTO nbsrKaitokano = null;
        // NBSR回答不可能DTO
        ContractDetailNbsrFukaitokanoDTO nbsrFukaitokano = null;
        // NBSR回答可能リスト
        List<ContractDetailNbsrKaitokanoDTO> nbsrKaitokanoList = new ArrayList<ContractDetailNbsrKaitokanoDTO>();
        // NBSR回答不可能リスト
        List<ContractDetailNbsrFukaitokanoDTO> nbsrFukaitokanoList = new ArrayList<ContractDetailNbsrFukaitokanoDTO>();

        for (ContractDetailNbsrInfoDTO nbsrDto : nbsrInfoList) {
            nbsrKaitokano = new ContractDetailNbsrKaitokanoDTO();
            // 回答可能
            if (ContractDetailConsts.KAITOU_KAISHO_KA.equals(nbsrDto.getAnscanFg())
                            && ContractDetailConsts.KAISHO_STATUS_0.equals(nbsrDto.getCanSt())
                            && ContractDetailConsts.KAITOU_STATUS_0.equals(nbsrDto.getAnsentSt())) {
                // 発信日
                nbsrKaitokano.setHashinbi(sdf.format(nbsrDto.getDspdcdDT()));
                // 内容
                nbsrKaitokano.setNaiyo(nbsrDto.getMsg());
                // 回答日
                if (!ContractDetailConsts.DEFAULT_DATE.equals(sdfs.format(nbsrDto.getAnsDt()))) {
                    nbsrKaitokano.setKaitoubi(sdf.format(nbsrDto.getAnsDt()));
                } else {
                    nbsrKaitokano.setKaitoubi(ContractDetailConsts.BLANK);
                }
                // 回答内容
                if (!StringUtils.isBlank(nbsrDto.getAnsCnt())) {
                    nbsrKaitokano.setKaitouNaiyou(nbsrDto.getAnsCnt());
                } else {
                    nbsrKaitokano.setKaitouNaiyou(ContractDetailConsts.BLANK);
                }
                // エラーワーニング連番
                nbsrKaitokano.setErrwRenban(nbsrDto.getErrwarNo());
                // ＮＢＳＲ連番
                nbsrKaitokano.setNbsrRenban(nbsrDto.getNbsNo());
                // 取得元区分
                nbsrKaitokano.setShutokumotoKubun(nbsrDto.getNbsrKbn());
                // 報告日
                nbsrKaitokano.setHokokubi(nbsrDto.getRepDt());
                nbsrKaitokanoList.add(nbsrKaitokano);
                // 回答不可
            } else if (ContractDetailConsts.KAITOU_KAISHO_FUKA.equals(nbsrDto.getAnscanFg())
                            && ContractDetailConsts.KAISHO_STATUS_0.equals(nbsrDto.getCanSt())
                            && ContractDetailConsts.KAITOU_STATUS_0.equals(nbsrDto.getAnsentSt())) {
                nbsrFukaitokano = new ContractDetailNbsrFukaitokanoDTO();
                // 発信日
                nbsrFukaitokano.setHashinbi(sdf.format(nbsrDto.getDspdcdDT()));
                // 内容
                nbsrFukaitokano.setNaiyo(nbsrDto.getMsg());
                nbsrFukaitokanoList.add(nbsrFukaitokano);
            }
        }
        nbsr.setNbsrFukano(nbsrFukaitokanoList);
        nbsr.setNbsrKano(nbsrKaitokanoList);
        resultDto.setNbsr(nbsr);

        // -------------------------------------------
        // 保障内容
        // -------------------------------------------
        // 保障内容DTO
        List<ContractDetailHoshonaiyoDTO> hoshonaiyoList = new ArrayList<ContractDetailHoshonaiyoDTO>();
        for (ContractDetailRiderInfoDTO rider : riderInfoList) {
            ContractDetailHoshonaiyoDTO hoshonaiyoDto = new ContractDetailHoshonaiyoDTO();
            if (ContractDetailConsts.CD_ARI.equals(rider.getRiderNo())
                            && !ContractDetailConsts.PACKAGE_KBN_10.equals(moshikomishoInfo.getPackageKbn())) {
                // パッケージ名取得条件マップ
                objParMap = new HashMap<String, Object>();
                objParMap.put(ContractDetailConsts.PACKAGE_CD, moshikomishoInfo.getPackageKbn());
                objParMap.put(ContractDetailConsts.ISFKND_CD, moshikomishoInfo.getShukeiyakuHokenshuruiCD());
                objParMap.put(ContractDetailConsts.PLT_CD, moshikomishoInfo.getShohingataCD());
                // 検索を実行する
                List<String> packageNameList = this.contractDetailMapper.getPackageName(objParMap);
                // 保険種類
                if (packageNameList != null && 0 < packageNameList.size()
                                && !ContractDetailConsts.BLANK.equals(packageNameList.get(0))) {
                    hoshonaiyoDto.setHokenshurui(packageNameList.get(0));
                } else {
                    hoshonaiyoDto.setHokenshurui(ContractDetailConsts.BLANK);
                }
            }
            if (StringUtils.isBlank(hoshonaiyoDto.getHokenshurui())) {
                if (rider.getShohimmeiKanji2() != null) {
                    hoshonaiyoDto.setHokenshurui(rider.getShohimmeiKanji2());
                }
            }
            // 主特区分
            if (ContractDetailConsts.CD_ARI.equals(rider.getRiderNo())) {
                hoshonaiyoDto.setKbn(ContractDetailConsts.HOKEN_KBN_SHU);
            } else {
                hoshonaiyoDto.setKbn(ContractDetailConsts.HOKEN_KBN_TOKU);
            }
            // 保険金額月額・日額区分
            hoshonaiyoDto.setKingakuKbn(getHokenKingakuKbn(rider.getShukeiyakuHokenshuruiCD()));

            // 保険金額、保険料
            if (ContractDetailConsts.HOKEN_TYPE_CD_CV.equals(rider.getShukeiyakuHokenshuruiCD())) {
                hoshonaiyoDto.setHokenkingaku(ContractDetailConsts.BLANK);
            } else if (!ContractDetailConsts.HALF_SPACE
                            .equals(StringUtils.substring(moshikomishoInfo.getCurrencyKubun(), 0, 1))
                            && !ContractDetailConsts.BLANK.equals(moshikomishoInfo.getCurrencyKubun())) {
                // 保険金額
                hoshonaiyoDto.setHokenkingaku(numDecimalFormat(
                                getAmountDivide(rider.getRiderHokenkin(), 100, 2,
                                                BigDecimal.ROUND_DOWN)));
                // 保険料
                hoshonaiyoDto.setHokenryo(numDecimalFormat(
                                getAmountDivide(rider.getRiderHokenryo(), 100, 2,
                                                BigDecimal.ROUND_DOWN)));
            } else {
                // 保険金額
                hoshonaiyoDto.setHokenkingaku(
                                numFormat(rider.getRiderHokenkin().toString()));
                // 保険料
                hoshonaiyoDto.setHokenryo(
                                numFormat(rider.getRiderHokenryo().toString()));
            }
            // 通貨単位
            if (!ContractDetailConsts.HALF_SPACE
                            .equals(StringUtils.substring(moshikomishoInfo.getCurrencyKubun(), 0, 1))
                            && !ContractDetailConsts.BLANK.equals(moshikomishoInfo.getCurrencyKubun())) {
                // 通貨マスターテーブル
                List<ContractDetailTsukaMasterInfoDTO> mCrc = this.contractDetailMapper
                                .getTsukaMastInfo(moshikomishoInfo.getCurrencyKubun());
                if (0 < mCrc.size()) {
                    hoshonaiyoDto.setHokenkingakuTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_1 +
                                    mCrc.get(0).getTsukaMeisho5() + ContractDetailConsts.DISP_MIGI_KATKO);
                    hoshonaiyoDto.setHokenryoTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_2 +
                                    mCrc.get(0).getTsukaMeisho5() + ContractDetailConsts.DISP_MIGI_KATKO);
                } else {
                    hoshonaiyoDto.setHokenkingakuTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_1
                                    + ContractDetailConsts.DISP_MIGI_KATKO);
                    hoshonaiyoDto.setHokenryoTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_2
                                    + ContractDetailConsts.DISP_MIGI_KATKO);
                }
            } else {
                hoshonaiyoDto.setHokenkingakuTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_3);
                hoshonaiyoDto.setHokenryoTitle(ContractDetailConsts.DISP_KINGAKU_LABEL_4);
            }
            hoshonaiyoList.add(hoshonaiyoDto);
        }
        resultDto.setHoshonaiyo(hoshonaiyoList);

        // -------------------------------------------
        // 査定状況の詳細
        // -------------------------------------------
        ContractDetailSateijokyoShosaiDTO sateijokyoShosaiDTO = new ContractDetailSateijokyoShosaiDTO();
        // 進捗状況
        sateijokyoShosaiDTO
                        .setStatus(getFinalResultName(StringUtils.substring(tSubviewInfo.getSaishuKekka(), 0, 1),
                                        sdfs.format(tSubviewInfo.getSeiritsuKakuteibi()),
                                        sdfs.format(tSubviewInfo.getFuseiritsuKakuteibi())));
        // 申込日(査定状況)
        sateijokyoShosaiDTO.setMoshikomibi(sdf.format(tSubviewInfo.getMoshikomibi()));
        // 初回入金日
        sateijokyoShosaiDTO.setShokainyukinbi(
                        shokainiukinbiInfo == null ? ContractDetailConsts.BLANK : sdf.format(shokainiukinbiInfo));
        // 告知書
        String serialBango = null;
        if (kokuchishoInfo == null) {
            serialBango = null;
        } else {
            serialBango = kokuchishoInfo.getSerialBango().trim();
        }
        String[] imuList = toListFromCommaString(ContractDetailConsts.IMU_LIST_KOKUCHISHO);
        if (serialBango == null || StringUtils.isBlank(serialBango)) {
            if (errwarInfo != null && 0 < errwarInfo.size()
                            && (imuList == null || chkStringItemInList(moshikomishoInfo.getImuCD1(), imuList))) {
                sateijokyoShosaiDTO.setKokuchisho(ContractDetailConsts.TOTATSU_MICYAKU);
            } else {
                sateijokyoShosaiDTO.setKokuchisho(ContractDetailConsts.DISP_HYPHEN);
            }
        } else if (serialBango.length() < 8) {
            sateijokyoShosaiDTO.setKokuchisho(ContractDetailConsts.BLANK);
        } else {
            sateijokyoShosaiDTO.setKokuchisho(stringFormatDate(StringUtils.substring(serialBango, 0, 8)));
        }
        // 団体加入確認
        sateijokyoShosaiDTO.setDantaikakunin(ContractDetailConsts.DISP_HYPHEN);
        if (dantaikakuninInfo == null || StringUtils.isBlank(dantaikakuninInfo)) {
            if (hojinErrwarInfo != null && 0 < hojinErrwarInfo.size()) {
                sateijokyoShosaiDTO.setDantaikakunin(ContractDetailConsts.TOTATSU_MICYAKU);
            }
        } else if (dantaikakuninInfo.length() < 8) {
            sateijokyoShosaiDTO.setDantaikakunin(ContractDetailConsts.BLANK);
        } else {
            if (!checkDateFormat(StringUtils.substring(dantaikakuninInfo, 0, 8))) {
                sateijokyoShosaiDTO.setDantaikakunin(StringUtils.substring(dantaikakuninInfo, 7, 15));
            } else {
                sateijokyoShosaiDTO.setDantaikakunin(stringFormatDate(StringUtils.substring(dantaikakuninInfo, 0, 8)));
            }
        }
        // 本社受付
        sateijokyoShosaiDTO.setHonshauketsuke(
                        stringFormatDate(StringUtils.substring(tSubviewInfo.getSerialBango(), 0, 8)));
        // 書類チェック・査定
        // 最終確認
        if (ankenkanriInfoList.size() > 0) {
            String sateiKekka = ankenkanriInfoList.get(0).getSateiKekka();
            // 書類チェック・査定、最終確認
            if (StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_03, sateiKekka)
                            || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_04, sateiKekka)
                            || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_05, sateiKekka)
                            || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_06, sateiKekka)
                            || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_07, sateiKekka)
                            || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_08, sateiKekka)) {
                /*
                 * 案件管理．査定結果が以下のいずれかの場合
                 *   "03"(不成立待ち)
                 *   "04"(不成立)
                 *   "05"(ｸｰﾘﾝｸﾞｵﾌ待ち)
                 *   "06"(ｸｰﾘﾝｸﾞｵﾌ)
                 *   "07"(謝絶待ち)
                 *   "08"(謝絶)
                 */
                // 書類チェック・査定
                sateijokyoShosaiDTO.setShoruiCheckSatei(ContractDetailConsts.BLANK);
                // 最終確認
                sateijokyoShosaiDTO.setSaishukakunin(ContractDetailConsts.BLANK);
            } else {
                /*
                 * 上記以外（案件管理．査定結果が以下のいずれか）の場合
                 *   初期値
                 *   "01"(成立待ち)
                 *   "02"(成立)
                 */
                // 共通処理(getStatus)を呼び出す。
                String hanteiKekka = getShoruicheckSateiStatus(
                                ankenkanriInfoList.get(0),
                                tSubviewInfo,
                                hojinAnkenkanriInfo,
                                shibuhyojiKakuteibi);
                // 書類チェック査定日付オブジェクトリスト
                List<Date> shoruiCheckSateiDateList = new ArrayList<Date>();
                // 案件管理．査定完了日
                shoruiCheckSateiDateList.add(ankenkanriInfoList.get(0).getSateiKanryobi());
                // 申込管理情報．ＳＰ査定＿日時の日付オブジェクト
                shoruiCheckSateiDateList.add(new Date(tSubviewInfo.getSpSateiNichiji().getTime()));
                if (hojinAnkenkanriInfo != null) {
                    // 法人案件管理．法人査定＿日時の日付オブジェクト
                    shoruiCheckSateiDateList.add(new Date(hojinAnkenkanriInfo.getHojinSateiNichiji().getTime()));
                }
                // 最終確認日付オブジェクトリスト
                List<Date> saishuKakuninDateList = new ArrayList<Date>();
                // 案件管理．最終チェック＿日時
                saishuKakuninDateList.add(new Date(ankenkanriInfoList.get(0).getSaishuCheckNichiji().getTime()));
                // 申込管理情報．ＳＰ査定＿日時の日付オブジェクト
                saishuKakuninDateList.add(new Date(tSubviewInfo.getSpSateiNichiji().getTime()));
                if (hojinAnkenkanriInfo != null) {
                    // 法人案件管理．法人査定＿日時の日付オブジェクト
                    saishuKakuninDateList.add(new Date(hojinAnkenkanriInfo.getHojinSateiNichiji().getTime()));
                }
                // 判定結果が"0"の場合
                if (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_0, hanteiKekka)) {
                    // NBSR/法人NBSRの存在チェック
                    if ((!hojinNbsrShibukaijitaijokakuninList.isEmpty() || !nbsrShibukaijitaijokakuninList.isEmpty())
                                    && chkKaitoMiketsu(shiburenkeiInfoList)) {
                        // 書類チェック・査定
                        sateijokyoShosaiDTO.setShoruiCheckSatei(ContractDetailConsts.SATEI_STATUS_KAITOMACHI);
                    } else {
                        // 書類チェック・査定
                        sateijokyoShosaiDTO.setShoruiCheckSatei(ContractDetailConsts.SATEI_STATUS_SATEICHU);
                    }
                    // 最終確認
                    sateijokyoShosaiDTO.setSaishukakunin(ContractDetailConsts.BLANK);
                    // 判定結果が"1"の場合
                } else if (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_1, hanteiKekka)) {
                    // NBSR/法人NBSRの存在チェック
                    if ((!hojinNbsrShibukaijitaijokakuninList.isEmpty() || !nbsrShibukaijitaijokakuninList.isEmpty())
                                    && chkKaitoMiketsu(shiburenkeiInfoList)) {
                        // 最終確認
                        sateijokyoShosaiDTO.setSaishukakunin(ContractDetailConsts.SAISHU_KAKUNIN_KAITOMACHI);
                    } else {
                        // NBSR/法人NBSR取得結果が存在しない場合
                        // 最終確認
                        sateijokyoShosaiDTO.setSaishukakunin(ContractDetailConsts.SAISHU_KAKUNIN_KAKUNINCHU);
                    }
                    // 書類チェック・査定
                    sateijokyoShosaiDTO.setShoruiCheckSatei(
                                    sdf.format(getMaxDate(shoruiCheckSateiDateList)));
                    // 判定結果が"2"の場合
                } else if (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_2, hanteiKekka)) {
                    // 書類チェック・査定
                    sateijokyoShosaiDTO.setShoruiCheckSatei(
                                    sdf.format(getMaxDate(shoruiCheckSateiDateList)));
                    // 最終確認
                    sateijokyoShosaiDTO.setSaishukakunin(
                                    sdf.format(getMaxDate(saishuKakuninDateList)));
                }
            }
            // 体況査定(査定状況)
            getTaikyoSateiInfo(tSubviewInfo, ankenkanriInfoList.get(0), sateijokyoShosaiDTO, shibuhyojiKakuteibi);
            // LINC
            sateijokyoShosaiDTO.setLinc(getLincStatusName(ankenkanriInfoList.get(0).getLincSateiStatus()));
        }
        // 成立日
        sateijokyoShosaiDTO.setSeiritsubi(ContractDetailConsts.BLANK);
        if (!ContractDetailConsts.DEFAULT_DATE.equals(sdfs.format(tSubviewInfo.getSeiritsuKakuteibi()))) {
            sateijokyoShosaiDTO.setSeiritsubi(sdf.format(tSubviewInfo.getSeiritsuKakuteibi()));
        } else if (!ContractDetailConsts.DEFAULT_DATE.equals(sdfs.format(tSubviewInfo.getFuseiritsuKakuteibi()))) {
            sateijokyoShosaiDTO.setSeiritsubi(ContractDetailConsts.DISP_HYPHEN);
        }
        // MR/SR確認(査定状況)
        // 所長確認
        boolean shochoCheckTotal = getShochocheckALL(shibuShochoCheckList,
                        shochoCheckzumiFlag == null ? shochoCheckFlag : shochoCheckzumiFlag, dbrFg,
                        juyoIkokakuninshoInfo);
        //所長チェック判定を設定
        if (shochoCheckTotal) {
            sateijokyoShosaiDTO.setMrsrkakunin(ContractDetailConsts.SHOCHO_KAKUNIN_KANRYO);
        } else {
            sateijokyoShosaiDTO.setMrsrkakunin(ContractDetailConsts.SHOCHO_KAKUNIN_MIRYO);
        }
        // ペンディング
        Integer diffdays = 0;
        // 長期ペンディング
        if (ContractDetailConsts.DEFAULT_DATE.equals(sdfs.format(tSubviewInfo.getSeiritsuKakuteibi()))
                        && ContractDetailConsts.DEFAULT_DATE
                                        .equals(sdfs.format(tSubviewInfo.getFuseiritsuKakuteibi()))) {
            diffdays = getNisuu(tSubviewInfo.getMoshikomibi(), this.contractDetailMapper.getSyoribi());
            sateijokyoShosaiDTO.setChokibendeingu(ContractDetailConsts.BLANK);
            if (diffdays != null) {
                sateijokyoShosaiDTO.setChokibendeingu(String.valueOf(diffdays));
            }
        } else {
            sateijokyoShosaiDTO.setChokibendeingu(ContractDetailConsts.BLANK);
        }
        // ガード文言
        sateijokyoShosaiDTO.setGuardMongon(ContractDetailConsts.BLANK);
        if (fuseritsuEnkiInfoList == null || fuseritsuEnkiInfoList.size() == 0) {
            if (!ContractDetailConsts.BLANK.equals(sateijokyoShosaiDTO.getChokibendeingu())
                            && ContractDetailConsts.CHOKI_PENDING_DT <= diffdays) {
                sateijokyoShosaiDTO.setGuardMongon(ContractDetailConsts.CHOKI_PENDING_WORD);
            }
        }
        resultDto.setSateishosai(sateijokyoShosaiDTO);

        // 保障内容詳細
        ContractDetailHoshonaiyoShosaiDTO hoshonaiyoShosaiDto = new ContractDetailHoshonaiyoShosaiDTO();
        // 円換算レート、総合計保険料(円換算)
        if (tssNyukinInfo != null && ((0 < tssNyukinInfo.getCwaBl() || 0 < tssNyukinInfo.getCwaBlDl())
                        || !ContractDetailConsts.DEFAULT_DATE.equals(
                                        sdfs.format(tSubviewInfo.getSeiritsuKakuteibi())))) {
            if (!ContractDetailConsts.HALF_SPACE2.equals(moshikomishoInfo.getCurrencyKubun())
                            && !ContractDetailConsts.CURRENCY_KBN_DATSUKA
                                            .equals(StringUtils.substring(moshikomishoInfo.getCurrencyKubun(),
                                                            moshikomishoInfo.getCurrencyKubun().length() - 1))
                            && ContractDetailConsts.ENKANSAN_TOKUYAKU_1
                                            .equals(moshikomishoInfo.getEnKansanHaraikomiTokuyakuKubun())) {
                hoshonaiyoShosaiDto.setEnkansanRate(tssNyukinInfo.getTts());
                BigDecimal amount = new BigDecimal(moshikomishoInfo.getHaraikomiHokenryo()
                                + moshikomishoInfo.getZennoHokenryo());
                amount = amount.multiply(new BigDecimal(tssNyukinInfo.getTts()));
                hoshonaiyoShosaiDto.setSogokeiHokenryoEnkansan(numFormat(
                                getAmountDivide(amount, 100, 0, BigDecimal.ROUND_HALF_UP)));
            }
        }
        // 前期期間
        hoshonaiyoShosaiDto.setZenKiKikan(
                        (riderInfoList != null && riderInfoList.size() > 0) ? riderInfoList.get(0).getHoshoKikanCD()
                                        : ContractDetailConsts.BLANK);
        // 最低支払保証期間
        hoshonaiyoShosaiDto.setSaiteiShiharaiHoshoKikan((riderInfoList != null && riderInfoList.size() > 0)
                        ? riderInfoList.get(0).getSaiteiShiharaiHoshoKikan()
                        : ContractDetailConsts.BLANK);
        // 遺族年金特約
        if (ContractDetailConsts.TOKUSHU_FLG_1.equals(moshikomishoInfo.getTokushuTokuFlag07())) {
            hoshonaiyoShosaiDto.setIzokuNenkinTokuyaku(ContractDetailConsts.DISP_FUGA_ARI);
        } else {
            hoshonaiyoShosaiDto.setIzokuNenkinTokuyaku(ContractDetailConsts.DISP_FUGA_NASI);
        }
        // リビングニーズ特約
        if (ContractDetailConsts.LIVING_NEEDS_1.equals(moshikomishoInfo.getLivingNeedsFlag())) {
            hoshonaiyoShosaiDto.setLivingNeedsTokuyaku(ContractDetailConsts.DISP_FUGA_ARI);
        } else {
            hoshonaiyoShosaiDto.setLivingNeedsTokuyaku(ContractDetailConsts.DISP_FUGA_NASI);
        }
        // 外貨建保険料振替額案内通知
        if (ContractDetailConsts.HOKEN_TYPE_CD_29.equals(moshikomishoInfo.getShukeiyakuHokenshuruiCD())
                        || ContractDetailConsts.HOKEN_TYPE_CD_98
                                        .equals(moshikomishoInfo.getShukeiyakuHokenshuruiCD())
                        || ContractDetailConsts.HOKEN_TYPE_CD_AL
                                        .equals(moshikomishoInfo.getShukeiyakuHokenshuruiCD())
                        || ContractDetailConsts.HOKEN_TYPE_CD_AM
                                        .equals(moshikomishoInfo.getShukeiyakuHokenshuruiCD())
                        || ContractDetailConsts.CURRENCY_KBN_DORU
                                        .equals(StringUtils.substring(moshikomishoInfo.getCurrencyKubun(),
                                                        moshikomishoInfo.getCurrencyKubun().length() - 1))) {
            if (ContractDetailConsts.HALF_SPACE.equals(moshikomishoInfo.getTokushuTokuFlag14())) {
                hoshonaiyoShosaiDto.setGaikadateHokenryoFurikaegakuAnnaiTsuchi(ContractDetailConsts.DISP_YOU);
            } else if (ContractDetailConsts.GAIKA_TSUCHI_1.equals(moshikomishoInfo.getTokushuTokuFlag14())) {

                hoshonaiyoShosaiDto.setGaikadateHokenryoFurikaegakuAnnaiTsuchi(ContractDetailConsts.DISP_FUYOU);
            } else {
                hoshonaiyoShosaiDto.setGaikadateHokenryoFurikaegakuAnnaiTsuchi(ContractDetailConsts.BLANK);
            }
        } else {
            hoshonaiyoShosaiDto.setGaikadateHokenryoFurikaegakuAnnaiTsuchi(ContractDetailConsts.BLANK);
        }
        // 保険料の自動貸付
        if (ContractDetailConsts.APL_KIBOU_FLG_0.equals(moshikomishoInfo.getAplkiboFlag())) {
            hoshonaiyoShosaiDto.setHokenryonoJidoKashitsuke(ContractDetailConsts.DISP_KIBOU_SINAI);
        } else if (ContractDetailConsts.APL_KIBOU_FLG_1.equals(moshikomishoInfo.getAplkiboFlag())) {
            hoshonaiyoShosaiDto.setHokenryonoJidoKashitsuke(ContractDetailConsts.DISP_KIBOU_SURU);
        } else {
            hoshonaiyoShosaiDto.setHokenryonoJidoKashitsuke(ContractDetailConsts.BLANK);
        }
        // 生存給付金支払日
        String seizonKyufukinShiharaibi = ContractDetailConsts.BLANK;
        if (!ContractDetailConsts.BLANK.equals(moshikomishoInfo.getSeizonKyufukinShiharaibi().trim())) {
            StringBuilder nenGetsu = new StringBuilder();
            nenGetsu.append(StringUtils.substring(moshikomishoInfo.getSeizonKyufukinShiharaibi(), 0, 2));
            nenGetsu.append(ContractDetailConsts.SLASH);
            nenGetsu.append(StringUtils.substring(moshikomishoInfo.getSeizonKyufukinShiharaibi(), 2));
            seizonKyufukinShiharaibi = nenGetsu.toString();
        }
        hoshonaiyoShosaiDto.setSeizonKyufukinShiharaibi(seizonKyufukinShiharaibi);
        // 介護前払特約
        if (ContractDetailConsts.KAIGO_MAEBARAI_1.equals(moshikomishoInfo.getTokushuTokuFlag02KaigoMaebarai())) {
            hoshonaiyoShosaiDto.setKaigoMaebaraiTokuyaku(ContractDetailConsts.DISP_FUGA_ARI);
        } else {
            hoshonaiyoShosaiDto.setKaigoMaebaraiTokuyaku(ContractDetailConsts.DISP_FUGA_NASI);
        }
        // 指定代理店請求特約
        if (ContractDetailConsts.LIVING_NEEDS_1.equals(moshikomishoInfo.getLivingNeedsFlag())
                        || ContractDetailConsts.LIVING_NEEDS_1
                                        .equals(moshikomishoInfo.getTokushuTokuFlag04shiteidairiseikyu())) {
            hoshonaiyoShosaiDto.setShiteiDairitenSeikyuTokuyaku(ContractDetailConsts.DISP_FUGA_ARI);
        } else {
            hoshonaiyoShosaiDto.setShiteiDairitenSeikyuTokuyaku(ContractDetailConsts.DISP_FUGA_NASI);
        }
        resultDto.setHoshoshosai(hoshonaiyoShosaiDto);
        // 契約内容詳細
        ContractDetailKeiyakunaiyoShosaiDTO keiyakunaiyoShosaiDTO = new ContractDetailKeiyakunaiyoShosaiDTO();
        // 保険種類
        keiyakunaiyoShosaiDTO.setHokenShurui(tSubviewInfo.getHokenshuruimei() == null ? ContractDetailConsts.BLANK
                        : tSubviewInfo.getHokenshuruimei());
        // 契約者カナ
        keiyakunaiyoShosaiDTO.setKeiyakushaKana(moshikomishoInfo.getKeiyakushameiKana());
        // 契約者
        keiyakunaiyoShosaiDTO.setKeiyakusha(moshikomishoInfo.getKeiyakushameiKanji());
        // 契約者生年月日／性別
        StringBuilder sb = new StringBuilder();
        String[] seinengappi = sdf.format(moshikomishoInfo.getKeiyakushaSeinengappi())
                        .split(ContractDetailConsts.SLASH);
        sb.append(seinengappi[0]);
        sb.append(ContractDetailConsts.NEN_KANJI);
        sb.append(seinengappi[1]);
        sb.append(ContractDetailConsts.GETSU_KANJI);
        sb.append(seinengappi[2]);
        sb.append(ContractDetailConsts.HI_KANJI);
        sb.append(ContractDetailConsts.SLASH);
        sb.append(getSeibetsuName(moshikomishoInfo.getKeiyakushaSeibetsu()));
        keiyakunaiyoShosaiDTO.setKeiyakushaSeinengappi(sb.toString());
        // 被保険者 カナ
        keiyakunaiyoShosaiDTO.setHihokenshaKana(moshikomishoInfo.getHihokenshameiKana1());
        // 被保険者
        keiyakunaiyoShosaiDTO.setHihokensha(moshikomishoInfo.getHihokenshameiKanji1());
        // 被保険者生年月日
        seinengappi = sdf.format(moshikomishoInfo.getHihokenshaSeinengappi1()).split(ContractDetailConsts.SLASH);
        sb = new StringBuilder();
        sb.append(seinengappi[0]);
        sb.append(ContractDetailConsts.NEN_KANJI);
        sb.append(seinengappi[1]);
        sb.append(ContractDetailConsts.GETSU_KANJI);
        sb.append(seinengappi[2]);
        sb.append(ContractDetailConsts.HI_KANJI);
        keiyakunaiyoShosaiDTO.setHihokenshaSeinengappi(sb.toString());
        // 性別
        keiyakunaiyoShosaiDTO.setHihokenshaSeibetsu(getSeibetsuName(moshikomishoInfo.getHihokenshaSeibetsu1()));
        // 告知日
        keiyakunaiyoShosaiDTO.setKokuchibi(kokuchiDt);
        // 契約日
        keiyakunaiyoShosaiDTO.setKeiyakubi(
                        moshikomishoInfo.getKeiyakubi() != null ? sdf.format(moshikomishoInfo.getKeiyakubi())
                                        : ContractDetailConsts.BLANK);
        // 選択方法
        keiyakunaiyoShosaiDTO.setSentakuHoho(getImuName(moshikomishoInfo.getImuCD1()));
        // 振込方法（経路）
        keiyakunaiyoShosaiDTO
                        .setFurikomiHohoKeiro(
                                        getSiharaiTypeNameKeiro(moshikomishoInfo.getShiharaikataHohoKeiroCD()));
        // 振込方法（回数）
        keiyakunaiyoShosaiDTO
                        .setFurikomiHohoKaisu(
                                        getSiharaiTypeNameKaisu(moshikomishoInfo.getShiharaikataHohoKairoCD()));

        // 受付人情報
        List<ContractDetailUketorininDTO> listkeyaku = new ArrayList<ContractDetailUketorininDTO>();
        for (ContractDetailUketorininInfoDTO uketorininInfo : uketorininInfoList) {
            ContractDetailUketorininDTO uketorininDto = new ContractDetailUketorininDTO();
            // 受取人種類
            uketorininDto.setUketorininShurui(uketorininInfo.getBeckndabbNk());
            // 氏名（カナ）
            uketorininDto.setShimeiKana(uketorininInfo.getBecNm());
            // 氏名（漢字）
            uketorininDto.setShimeiKanji(uketorininInfo.getBecNk());
            // 受取割合
            uketorininDto.setUketoriWariai(uketorininInfo.getBecSp() == 0 ? ContractDetailConsts.BLANK
                            : String.valueOf(uketorininInfo.getBecSp()));
            // 続柄
            uketorininDto.setZokugara(getZokugaraName(uketorininInfo.getBecrelCd()));
            // 性別
            uketorininDto.setUketorininSeibetsu(getSeibetsuName(uketorininInfo.getBecsexCd()));
            // 生年月日
            seinengappi = sdf.format(uketorininInfo.getBecbirDt()).split(ContractDetailConsts.SLASH);
            sb = new StringBuilder();
            sb.append(seinengappi[0]);
            sb.append(ContractDetailConsts.NEN_KANJI);
            sb.append(seinengappi[1]);
            sb.append(ContractDetailConsts.GETSU_KANJI);
            sb.append(seinengappi[2]);
            sb.append(ContractDetailConsts.HI_KANJI);
            uketorininDto.setUketoriSeinengappi(sb.toString());
            listkeyaku.add(uketorininDto);
        }
        keiyakunaiyoShosaiDTO.setUketorininList(listkeyaku);
        // 特則区分
        keiyakunaiyoShosaiDTO.setTokusokuKbn(getTokusokuKbn(moshikomishoInfo.getTokusokuKubun()));

        // 契約者通信先エリア
        ContractDetailTsushinsakiDTO tsushinsakiDto = new ContractDetailTsushinsakiDTO();
        // 郵便番号(契約者)
        tsushinsakiDto.setYubimBango(moshikomishoInfo.getKeiyakushaYubinbango());
        // TEL(契約者)
        tsushinsakiDto.setTel(moshikomishoInfo.getKeiyakushaDenwabango());
        // FAX(契約者)
        tsushinsakiDto.setFax(moshikomishoInfo.getKeiyakushaFAXBango());
        // 住所（カナ）(契約者)
        sb = new StringBuilder();
        sb.append(moshikomishoInfo.getKeiyakushaJushoKana1());
        sb.append(moshikomishoInfo.getKeiyakushaJushoKana2());
        sb.append(moshikomishoInfo.getKeiyakushaJushoKana3());
        tsushinsakiDto.setJushoKana(sb.toString());
        // 住所（漢字）(契約者)
        sb = new StringBuilder();
        sb.append(moshikomishoInfo.getKeiyakushaJusho1());
        sb.append(moshikomishoInfo.getKeiyakushaJusho2());
        sb.append(moshikomishoInfo.getKeiyakushaJusho3());
        tsushinsakiDto.setJushoKanji(sb.toString());
        // 携帯電話(契約者)
        tsushinsakiDto.setKeitaiDenwaBango(moshikomishoInfo.getKeiyakushaMobileNo());
        // E-mail(契約者)
        tsushinsakiDto.setEMail(moshikomishoInfo.getKeiyakushaMailAdd());
        // メール区分(契約者)
        tsushinsakiDto.setMailKubun(moshikomishoInfo.getKeiyakushaMailKbn());
        keiyakunaiyoShosaiDTO.setKeiyakushaTsushinsaki(tsushinsakiDto);

        // 被保険者通信先エリア
        ContractDetailTsushinsakiDTO tsushinsakiHihokenshaDto = new ContractDetailTsushinsakiDTO();
        // 郵便番号(被保険者)
        tsushinsakiHihokenshaDto.setYubimBango(moshikomishoInfo.getHihokenshaYubinbango1());
        // TEL(被保険者)
        tsushinsakiHihokenshaDto.setTel(moshikomishoInfo.getHihokenshaDenwabango1());
        // FAX(被保険者)
        tsushinsakiHihokenshaDto.setFax(moshikomishoInfo.getHihokenshaFAXBango1());
        // 住所（カナ）(被保険者)
        sb = new StringBuilder();
        sb.append(moshikomishoInfo.getHihokenshaJushoKana1());
        sb.append(moshikomishoInfo.getHihokenshaJushoKana2());
        sb.append(moshikomishoInfo.getHihokenshaJushoKana3());
        tsushinsakiHihokenshaDto.setJushoKana(sb.toString());
        // 住所（漢字）(被保険者)
        sb = new StringBuilder();
        sb.append(moshikomishoInfo.getHihokenshaJusho1());
        sb.append(moshikomishoInfo.getHihokenshaJusho2());
        sb.append(moshikomishoInfo.getHihokenshaJusho3());
        tsushinsakiHihokenshaDto.setJushoKanji(sb.toString());
        // 携帯電話(被保険者)
        tsushinsakiHihokenshaDto.setKeitaiDenwaBango(moshikomishoInfo.getHihokenshaMobileNo1());
        // E-mail(被保険者)
        tsushinsakiHihokenshaDto.setEMail(moshikomishoInfo.getHihokenshaMailAdd1());
        // メール区分(被保険者)
        tsushinsakiHihokenshaDto.setMailKubun(moshikomishoInfo.getHihokenshaMailKbn());
        keiyakunaiyoShosaiDTO.setHihokenshaTsushinsaki(tsushinsakiHihokenshaDto);
        resultDto.setKeiyakushousai(keiyakunaiyoShosaiDTO);

        // 代理店情報詳細
        ContractDetailDairitenjohoShosaiDTO dairitenShosaiDto = new ContractDetailDairitenjohoShosaiDTO();
        // 第一取扱者
        ContractDetailToriatsukaishaDTO daiiToriatsukaishaDto = new ContractDetailToriatsukaishaDTO();
        if (dairitenInfo1 != null) {
            // 代理店コード(第一取扱者)
            daiiToriatsukaishaDto.setDairitenCD(dairitenInfo1.getFinName());
            // 拠点コード(第一取扱者)
            daiiToriatsukaishaDto.setKyotenCD(dairitenInfo1.getBrcName());
            // 取扱者(第一取扱者)
            daiiToriatsukaishaDto.setToriatsukaishaMei(dairitenInfo1.getShimei());
            // 営業部コード(第一取扱者)
            daiiToriatsukaishaDto.setEigyobuCD(dairitenInfo1.getShozokumei());
        }
        dairitenShosaiDto.setDaiichiToriatsukaisha(daiiToriatsukaishaDto);
        // 第二取扱者
        ContractDetailToriatsukaishaDTO dainiToriatsukaishaDto = new ContractDetailToriatsukaishaDTO();
        if (dairitenInfo2 != null) {
            // 代理店コード(第二取扱者)
            dainiToriatsukaishaDto.setDairitenCD(dairitenInfo2.getFinName());
            // 拠点コード(第二取扱者)
            dainiToriatsukaishaDto.setKyotenCD(dairitenInfo2.getBrcName());
            // 取扱者(第二取扱者)
            dainiToriatsukaishaDto.setToriatsukaishaMei(dairitenInfo2.getShimei());
            // 営業部コード(第二取扱者)
            dainiToriatsukaishaDto.setEigyobuCD(dairitenInfo2.getShozokumei());
        }
        dairitenShosaiDto.setDainiToriatsukaisha(dainiToriatsukaishaDto);
        resultDto.setDairitenshosai(dairitenShosaiDto);
        return resultDto;
    }

    /**
     * NBSR回答
     *
     * @param answerParameterDto 回答パラメーターDTO
     * @throws Exception 例外
     */
    @Override
    @Description("NBSR回答登録処理")
    public void answer(ContractDetailAnswerParameterDTO answerParameterDto) throws Exception {
        HashMap<String, Object> objParMap = null;
        // 証券番号
        String shokemBango = answerParameterDto.getShokenBango();
        // 取得元区分定数
        String shutokumotoKubun = answerParameterDto.getShutokumotoKubun();
        // エラーワーニング連番
        String errwRenban = answerParameterDto.getErrwRenban();
        // NBSR連番
        String nbsrRenban = answerParameterDto.getNbsrRenban();
        // NBSR回答済チェック
        objParMap = new HashMap<String, Object>();
        objParMap.put(ContractDetailConsts.SHUTOKUMOTO_KUBUN, shutokumotoKubun);
        objParMap.put(ContractDetailConsts.SHOKEM_BANGO, shokemBango);
        objParMap.put(ContractDetailConsts.ERRWRENBAN, errwRenban);
        objParMap.put(ContractDetailConsts.NBSRRENBAN, nbsrRenban);
        // 回答済件数
        int cunt = this.contractDetailMapper.nbsrAnsCheck(objParMap);
        if (cunt > 0) {
            throw new MobileException(MessageCode.MSG1004W,
                            this.messageSource.getMessage(MessageCode.MSG1004W,
                                            new Object[] { ContractDetailConsts.MSG_KAITO }, null));
        }

        // 支部連携（通番）の採番処理
        String cnsNo = this.contractDetailMapper.numberingCnsNo(shokemBango);
        if (cnsNo != null) {
            cnsNo = String.valueOf(Integer.parseInt(cnsNo) + 1);
        } else {
            cnsNo = String.valueOf(1);
        }

        // 支部連携の保存（登録）処理
        ContractDetailAnswerDTO answerDto = new ContractDetailAnswerDTO();
        answerDto.setTsuban(cnsNo);
        answerDto.setErrwRenban(errwRenban);
        answerDto.setNbsrRenban(nbsrRenban);
        answerDto.setShokenBango(shokemBango);
        answerDto.setUserID(answerParameterDto.getUserID());
        answerDto.setUserName(answerParameterDto.getUserName());
        answerDto.setKaitoNaiyo(answerParameterDto.getKaitoNaiyo());
        if (ContractDetailConsts.SHUTOKUMOTO_KUBUN_1.equals(shutokumotoKubun)) {
            // 取扱報告書区分
            answerDto.setToriatukaiKbn(ContractDetailConsts.TORIHIKI_INFO_KBN1);
            // 報告日
            answerDto.setHokokubi(ContractDetailConsts.DEFAULT_DATE_DISP_HYPHEN);
        } else {
            // 取扱報告書区分
            answerDto.setToriatukaiKbn(ContractDetailConsts.TORIHIKI_INFO_KBN2);
            // 報告日
            answerDto.setHokokubi(answerParameterDto.getHokokubi().replace(ContractDetailConsts.SLASH,
                            ContractDetailConsts.DISP_HYPHEN));
        }
        this.contractDetailMapper.insertTOfiCoop(answerDto);
    }

    /**
     * 変数.回答済み情報に"0"(回答未済)が存在するかどうかを返す。
     *
     * @param kaitoZumiInfoList 回答済み情報
     * @return true:"0"(回答未済)が存在する/false:"0"(回答未済)が存在しない。
     */
    private boolean chkKaitoMiketsu(List<String> kaitoZumiInfoList) {
        for (String kaitozumiInfo : kaitoZumiInfoList) {
            if (ContractDetailConsts.KAITOU_STATUS_0.equals(kaitozumiInfo)) {
                return true;
            }
        }
        return false;
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
        if (ContractDetailConsts.BLANK.equals(torihou)) {
            torihou = ContractDetailConsts.SHOCHO_CHECK_MIRYO;
        }
        if (ContractDetailConsts.BLANK.equals(iko)) {
            iko = ContractDetailConsts.SHOCHO_CHECK_MIRYO;
        }
        if (ContractDetailConsts.BLANK.equals(jusetsu)) {
            jusetsu = ContractDetailConsts.SHOCHO_CHECK_MIRYO;
        }

        // すべて未スキャンの場合、falseを返す
        if (torihou == null && iko == null && jusetsu == null) {
            return false;
            //取報・意向・重説の中に、一つでも所長チェック未了のものがあれば、falseを返す
        } else if ((ContractDetailConsts.SHOCHO_CHECK_MIRYO.equals(torihou)
                        && !list.contains(ContractDetailConsts.SHOCHO_CHECK_KANRYO1))
                        || (ContractDetailConsts.SHOCHO_CHECK_MIRYO.equals(iko)
                                        && !list.contains(ContractDetailConsts.SHOCHO_CHECK_KANRYO2))
                        || (ContractDetailConsts.SHOCHO_CHECK_MIRYO.equals(jusetsu)
                                        && !list.contains(ContractDetailConsts.SHOCHO_CHECK_KANRYO3))) {
            return false;
            //上記以外
        } else {
            return true;
        }
    }

    /**
     * 体況査定結果を元に体況査定情報を返す。
     *
     * @return 体況査定情報
     */
    private void getTaikyoSateiInfo(ContractDetailTSubviewDTO tsubView, ContractDetailAnkenkanriInfoDTO ankenkanriDto,
                    ContractDetailSateijokyoShosaiDTO sateijokyoShosaiDTO, Date shibuhyojiKakuteibi) {

        SimpleDateFormat sdfs = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMMDD_SLASH);
        SimpleDateFormat sdf = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMD_SLASH);
        // 案件管理．再保険ステータス＝"02"(照会中)の場合
        if (StringUtils.equals(ContractDetailConsts.SAIHOKEN_STATUS_CD_02,
                        ankenkanriDto.getSaihokenStatus())) {
            // 再保照会中
            sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.SATEI_STATUS_SAIHOKENSHOKAICHU);
            sateijokyoShosaiDTO.setTaikyosatei2(ContractDetailConsts.BLANK);
            // 案件管理．人為査定工程＝"14"(医事照会)の場合
        } else if (StringUtils.equals(ContractDetailConsts.JINISATEI_KOUTEI_CD_14,
                        ankenkanriDto.getJiniSateiKotei())) {
            // 医事照会中
            sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.SATEI_STATUS_IZISHOKAICHU);
            sateijokyoShosaiDTO.setTaikyosatei2(ContractDetailConsts.BLANK);
        } else {
            // 体況査定＿支部開示日時が"1900/01/01"の場合
            if (ContractDetailConsts.DEFAULT_DATE
                            .equals(tsubView.getTaikyoSateiShibuKaijiNichiji() != null
                                            ? sdfs.format(tsubView.getTaikyoSateiShibuKaijiNichiji())
                                            : tsubView.getTaikyoSateiShibuKaijiNichiji())) {
                // 空文字
                sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.BLANK);
                sateijokyoShosaiDTO.setTaikyosatei2(ContractDetailConsts.BLANK);
            } else {
                // 体況査定＿支部開示日時が"1900/01/01"以外の場合
                // 体況査定結果
                String sateiKekka = tsubView.getTaikyoSateiKekka();
                // 体況査定結果 = "11"（無条件、引受可、成立（無条件）)の場合
                if (ContractDetailConsts.TAIKYO_SASTEI_KEKKA_MUJYOKEN.equals(sateiKekka)) {
                    sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.TAIKYO_SASTEI_MUJYOKEN);
                    sateijokyoShosaiDTO.setTaikyosatei2(
                                    sdf.format(tsubView.getTaikyoSateiShibuKaijiNichiji()));
                    // 体況査定結果 = "12"（特別条件、条件付引受、成立（特別条件））かつ
                    // 支部表示確定日が"1900/01/01"でない場合
                } else if (ContractDetailConsts.TAIKYO_SASTEI_KEKKA_TOKUBETSU.equals(sateiKekka)
                                && !ContractDetailConsts.DEFAULT_DATE
                                                .equals(shibuhyojiKakuteibi != null ? sdfs.format(shibuhyojiKakuteibi)
                                                                : shibuhyojiKakuteibi)) {
                    sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.TAIKYO_SASTEI_TOKUBETSU);
                    sateijokyoShosaiDTO.setTaikyosatei2(
                                    sdf.format(tsubView.getTaikyoSateiShibuKaijiNichiji()));
                    /*
                     * 体況査定結果が上１桁＝"R"（謝絶扱い）の場合
                     *   かつ
                     * 支部表示確定日が"1900/01/01"でない場合
                     */
                } else if (ContractDetailConsts.TAIKYO_SASTEI_KEKKA_SYAZETSUATSUKAI
                                .equals(StringUtils.substring(sateiKekka, 0, 1))
                                && !ContractDetailConsts.DEFAULT_DATE
                                                .equals(shibuhyojiKakuteibi != null ? sdfs.format(shibuhyojiKakuteibi)
                                                                : shibuhyojiKakuteibi)) {
                    sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.TAIKYO_SASTEI_SYAZETSU);
                    sateijokyoShosaiDTO.setTaikyosatei2(
                                    sdf.format(tsubView.getTaikyoSateiShibuKaijiNichiji()));
                    // 上記以外
                } else {
                    sateijokyoShosaiDTO.setTaikyosatei1(ContractDetailConsts.BLANK);
                    sateijokyoShosaiDTO.setTaikyosatei2(ContractDetailConsts.BLANK);
                }
            }
        }
    }

    /**
     * LINC査定ステータスコードを元にLINC査定ステータスの名称を返す。
     *
     * @param code LINC査定ステータスコード
     * @return LINC査定ステータス名称
     * @exception  Exception 異常
     */
    private String getLincStatusName(String code) throws Exception {
        String meisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.LINC_STATUS_00.equals(code)
                        || ContractDetailConsts.LINC_STATUS_09.equals(code)) {
            // 申込管理情報．LINC査定ステータス＝"00"（不要）、"09"（完了）
            // LINC査定ステータス："－"
            meisho = ContractDetailConsts.DISP_HYPHEN;
        } else if (ContractDetailConsts.LINC_STATUS_01.equals(code)
                        || ContractDetailConsts.LINC_STATUS_02.equals(code)) {
            // 申込管理情報．LINC査定ステータス＝"01"（LINC査定中(LINC未了)）、"02"（LINC査定中(他社契約あり、回付)）
            // LINC査定ステータス："確認中"
            meisho = ContractDetailConsts.DISP_KAKUNINCHU;
        } else if (ContractDetailConsts.LINC_STATUS_03.equals(code)
                        || ContractDetailConsts.LINC_STATUS_04.equals(code)
                        || ContractDetailConsts.LINC_STATUS_08.equals(code)) {
            // 申込管理情報．LINC査定ステータス＝"03"（他社照会中）、"04"（査定中）、"08"（完了（対応済み））
            // LINC査定ステータス：""（空文字）
            meisho = ContractDetailConsts.BLANK;
        }
        return meisho;
    }

    /**
     * 特則区分を元に編集文字列を返す。
     *
     * @param code 特則区分
     * @return 編集文字列
     * @exception  Exception 異常
     */
    private String getTokusokuKbn(String code) throws Exception {
        String result = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.TOKUSOKU_KBN_1.equals(code)) {
            result = ContractDetailConsts.DISP_KEIYAKUSYA;
        } else if (ContractDetailConsts.TOKUSOKU_KBN_2.equals(code)) {
            result = ContractDetailConsts.DISP_HOUJIN;
        }
        return result;
    }

    /**
     * 性別コードを元に性別の名称を返す。
     *
     * @param seibetsuCD 性別コード
     * @return 性別名称
     * @exception  Exception 異常
     */
    private String getSeibetsuName(String seibetsuCD) throws Exception {
        String seibetsuMeisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.SEIBETSU_CD_M.equals(seibetsuCD)) {
            seibetsuMeisho = ContractDetailConsts.SEIBETSU_DANSEI;
        } else if (ContractDetailConsts.SEIBETSU_CD_F.equals(seibetsuCD)) {
            seibetsuMeisho = ContractDetailConsts.SEIBETSU_JYOSEI;
        } else if (ContractDetailConsts.SEIBETSU_CD_C.equals(seibetsuCD)) {
            seibetsuMeisho = ContractDetailConsts.SEIBETSU_HOUJIN;
        }
        return seibetsuMeisho;
    }

    /**
     * 続柄コードを元に続柄の名称を返す。
     *
     * @param zokugaraCD 続柄コード
     * @return 続柄名称
     * @exception  Exception 異常
     */
    private String getZokugaraName(String zokugaraCD) throws Exception {
        String zokugaraMeisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.ZOKUGARA_CD_01.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_HONNIN;
        } else if (ContractDetailConsts.ZOKUGARA_CD_02.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_HAIGUSYA;
        } else if (ContractDetailConsts.ZOKUGARA_CD_03.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_OYA;
        } else if (ContractDetailConsts.ZOKUGARA_CD_04.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_KO;
        } else if (ContractDetailConsts.ZOKUGARA_CD_05.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_KYODAI_SIMAI;
        } else if (ContractDetailConsts.ZOKUGARA_CD_06.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_SOFUBO;
        } else if (ContractDetailConsts.ZOKUGARA_CD_07.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_MAGO;
        } else if (ContractDetailConsts.ZOKUGARA_CD_08.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_JYUGYOIN;
        } else if (ContractDetailConsts.ZOKUGARA_CD_09.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_YAKUIN;
        } else if (ContractDetailConsts.ZOKUGARA_CD_10.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_SOZOKUIN;
        } else if (ContractDetailConsts.ZOKUGARA_CD_99.equals(zokugaraCD)) {
            zokugaraMeisho = ContractDetailConsts.ZOKUGARA_SONOTA;
        }
        return zokugaraMeisho;
    }

    /**
     * 支払方法コードを元に支払方法の名称を返す。
     * ※ 回数
     * @param siharaiCD 支払方法コード
     * @return 支払方法名称
     * @exception  Exception 異常
     */
    private String getSiharaiTypeNameKaisu(String siharaiCD) throws Exception {
        String siharaiMeisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.SIHARAI_CD_01.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_TSUKI;
        } else if (ContractDetailConsts.SIHARAI_CD_06.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_HANTOSI;
        } else if (ContractDetailConsts.SIHARAI_CD_12.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_NEN;
        }
        return siharaiMeisho;
    }

    /**
     * 支払方法コードを元に支払方法の名称を返す。
     * ※ 経路
     * @param siharaiCD 支払方法コード
     * @return 支払方法名称
     * @exception  Exception 異常
     */
    private String getSiharaiTypeNameKeiro(String siharaiCD) throws Exception {
        String siharaiMeisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.SIHARAI_CD_P.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_FURI;
        } else if (ContractDetailConsts.SIHARAI_CD_G.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_DAN;
        } else if (ContractDetailConsts.SIHARAI_CD_T.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_TOKU;
        } else if (ContractDetailConsts.SIHARAI_CD_S.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_ICHI;
        } else if (ContractDetailConsts.SIHARAI_CD_5.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_KYO;
        } else if (ContractDetailConsts.SIHARAI_CD_7.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_JI;
        } else if (ContractDetailConsts.SIHARAI_CD_C.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_CON;
        } else if (ContractDetailConsts.SIHARAI_CD_1.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_SO_NEN;
        } else if (ContractDetailConsts.SIHARAI_CD_3.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_SO_TSUKI;
        } else if (ContractDetailConsts.SIHARAI_CD_8.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_MAE;
        } else if (ContractDetailConsts.SIHARAI_CD_A.equals(siharaiCD)) {
            siharaiMeisho = ContractDetailConsts.SIHARAI_SHU;
        }
        return siharaiMeisho;
    }

    /**
     * 医務コードを元に医務の名称を返す。
     *
     * @param imuCD 医務コード
     * @return 医務名称
     * @exception  Exception 異常
     */
    private String getImuName(String imuCD) throws Exception {
        String imuMeisho = ContractDetailConsts.BLANK;
        if (ContractDetailConsts.HALF_SPACE.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_FUTSU;
        } else if (ContractDetailConsts.IMU_CD_1.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_SYAI;
        } else if (ContractDetailConsts.IMU_CD_2.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_SHOKUTAKUI;
        } else if (ContractDetailConsts.IMU_CD_3.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_KENKOUSHOMEI;
        } else if (ContractDetailConsts.IMU_CD_4.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_MENSETHUSI;
        } else if (ContractDetailConsts.IMU_CD_5.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_NINGENDOTKU;
        } else if (ContractDetailConsts.IMU_CD_6.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_OPAI;
        } else if (ContractDetailConsts.IMU_CD_7.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_HENKAN;
        } else if (ContractDetailConsts.IMU_CD_8.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_HENKAKU;
        } else if (ContractDetailConsts.IMU_CD_9.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_SHOKUTAKUI_B;
        } else if (ContractDetailConsts.IMU_CD_A.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_TSUCHISYO_A;
        } else if (ContractDetailConsts.IMU_CD_B.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_TSUCHISYO_B;
        } else if (ContractDetailConsts.IMU_CD_0.equals(imuCD)) {
            imuMeisho = ContractDetailConsts.IMU_KOKUCHI;
        }
        return imuMeisho;
    }

    /**
     * 基準日1と基準日2の間の日数を計算する。
     * @param kijunbi1  基準日1
     * @param kijunbi2  基準日2
     * @return 日数
     */
    private Integer getNisuu(Date kijunbi1, Date kijunbi2) {
        // カレンダー
        Calendar cal = Calendar.getInstance();
        // 日数
        int nisuu = 0;
        // 引数チェック(不正な引数の場合NULLを返す。)
        // 基準日1と基準日2の前後関係をチェックする。
        if (kijunbi2.compareTo(kijunbi1) < 0) {
            return null;
        }
        // 基準日1と基準日2のミリ秒値を取得する。
        cal.clear();
        cal.setTime(kijunbi1);
        long time1 = cal.getTimeInMillis();
        cal.clear();
        cal.setTime(kijunbi2);
        long time2 = cal.getTimeInMillis();
        // 基準日2と基準日1の日数を計算する。
        nisuu = (int) ((time2 - time1) / ContractDetailConsts.DAY_MILLISECONDS);
        // 両端を含むの場合、日数計算値に1を加算して返す。
        return Integer.valueOf(nisuu + 1);
    }

    /**
     * 日付オブジェクトリストの中から最大日付を返す
     * @param dtList 日付オブジェクトリスト
     * @return 最大日付オブジェクト
     */
    private Date getMaxDate(List<Date> dtList) {
        Date retVal = null;
        for (Date dt : dtList) {
            if (retVal == null || 0 < dt.compareTo(retVal)) {
                retVal = dt;
            }
        }
        return retVal;
    }

    /**
     * 書類チェック・査定の進捗状況を返す。
     * @param tAnkAdmDto 案件管理Dto
     * @param tSubAdmDto 申込管理Dto
     * @param tCmpAdmDto 法人案件管理Dto
     * @param shibuhyojiKakuteibi 支部表示確定日
     * @return 書類チェック・査定の進捗状況
     *          "0"：書類ﾁｪｯｸ・査定未完
     *          "1"：書類ﾁｪｯｸ・査定完、最終チェック未完
     *          "2"：書類ﾁｪｯｸ・査定完、最終チェック完
     */
    private String getShoruicheckSateiStatus(ContractDetailAnkenkanriInfoDTO tAnkAdmDto,
                    ContractDetailTSubviewDTO tSubAdmDto,
                    ContractDetailHojinAnkenkanriInfoDTO tCmpAdmDto, Date shibuhyojiKakuteibi) {

        SimpleDateFormat sdf = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMMDD_SLASH);
        String hanteiKekka = ContractDetailConsts.BLANK;

        // 書類ﾁｪｯｸ・査定の判定を行う
        /*
         * 以下の条件にすべて一致しているかどうかを判定する
         *   案件管理．不備チェックｽﾃｰﾀｽ＝"09"(完了)
         *   案件管理．１次査定ｽﾃｰﾀｽ＝"09"(完了)
         *   案件管理．２次査定ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         *   案件管理．ＬＩＮＣ査定ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         *   案件管理．医事照会ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         *   案件管理．社医査定ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         *   申込管理．体況査定＿支部開示日時≠"1900/01/01"（初期値）
         *   申込管理．体況査定結果≠"半角スペース（未決）"  かつ  "11"(無条件、引受可、成立（無条件）の場合
         *     入力値．特別条件通知あり  かつ
         *     特別条件通知．支部表示確定日≠"1900/01/01"（初期値）
         *   申込管理．ＳＰ査定ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         *   法人案件管理ありの場合
         *     法人案件管理．法人査定ｽﾃｰﾀｽ＝"00"(初期値)、"09"(完了)
         */
        if (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09, tAnkAdmDto.getFubiCheckStatus())
                        && StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                        tAnkAdmDto.getIchijiSateiStatus())
                        && (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                        tAnkAdmDto.getNijiSateiStatus())
                                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                        tAnkAdmDto.getNijiSateiStatus()))
                        && (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                        tAnkAdmDto.getLincSateiStatus())
                                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                        tAnkAdmDto.getLincSateiStatus()))
                        && (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                        tAnkAdmDto.getIjiShokaiStatus())
                                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                        tAnkAdmDto.getIjiShokaiStatus()))
                        && (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                        tAnkAdmDto.getShaiSateiStatus())
                                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                        tAnkAdmDto.getShaiSateiStatus()))
                        && !StringUtils.equals(ContractDetailConsts.DEFAULT_DATE,
                                        sdf.format(tSubAdmDto.getTaikyoSateiShibuKaijiNichiji()))
                        && ((StringUtils.equals(ContractDetailConsts.TAIKYOSATEI_SHOKICHI,
                                        tSubAdmDto.getTaikyoSateiKekka())
                                        || StringUtils.equals(ContractDetailConsts.TAIKYOSATEIKEKKA_11,
                                                        tSubAdmDto.getTaikyoSateiKekka()))
                                        || (shibuhyojiKakuteibi != null
                                                        && !StringUtils.equals(ContractDetailConsts.DEFAULT_DATE,
                                                                        sdf.format(shibuhyojiKakuteibi))))
                        && (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                        tSubAdmDto.getSpSateiStatus())
                                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                        tSubAdmDto.getSpSateiStatus()))
                        && (tCmpAdmDto == null
                                        || (StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00,
                                                        tCmpAdmDto.getHojinSateiStatus())
                                                        || StringUtils.equals(
                                                                        ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                                                        tCmpAdmDto.getHojinSateiStatus())))) {
            // 上記条件に一致する場合
            // 判定結果 ＝ 1：書類ﾁｪｯｸ・査定完、最終チェック未完
            hanteiKekka = ContractDetailConsts.SHORUICHECK_SATEI_1;
        } else {
            // 上記条件に一致しない場合
            // 判定結果 ＝ 0：書類ﾁｪｯｸ・査定未完
            hanteiKekka = ContractDetailConsts.SHORUICHECK_SATEI_0;
        }
        // 最終チェックの判定を行う
        /*
         * 以下の条件にすべて一致しているかどうかを判定する
         *   案件管理．最終チェックｽﾃｰﾀｽ＝"00"(初期値)　または　"09"(完了)
         *   案件管理．案件ｽﾃｰﾀｽ
         *      ＝"03"(初回源泉承諾)、"04"(成立・不成立待ち)、"05"(成立・不成立決着)、"06"(成立後書類受付)
         *   案件管理．案件ｽﾃｰﾀｽ＿日時≠"1900/01/01"（初期値）
         */
        if ((StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_00, tAnkAdmDto.getSaishuCheckStatus())
                        || StringUtils.equals(ContractDetailConsts.SHORUICHECK_SATEI_STATUS_09,
                                        tAnkAdmDto.getSaishuCheckStatus()))
                        && (StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_03,
                                        tAnkAdmDto.getAnkenStatus())
                                        || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_04,
                                                        tAnkAdmDto.getAnkenStatus())
                                        || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_05,
                                                        tAnkAdmDto.getAnkenStatus())
                                        || StringUtils.equals(ContractDetailConsts.ANKENKANRI_SATEIKEKKA_06,
                                                        tAnkAdmDto.getAnkenStatus()))
                        && !StringUtils.equals(ContractDetailConsts.DEFAULT_DATE,
                                        sdf.format(tAnkAdmDto.getAnkenStatusNichiji()))) {
            // 上記条件に一致する場合
            // 判定結果 ＝ 2：書類ﾁｪｯｸ・査定完、最終チェック完
            hanteiKekka = ContractDetailConsts.SHORUICHECK_SATEI_2;
        }
        return hanteiKekka;
    }

    /**
     * 日付の妥当性チェックを行います。<br>
     * 指定した日付文字列（yyyyMMdd）であるかチェックします。
     *
     * @param strDate
     *            チェック対象の文字列
     */
    private boolean checkDateFormat(String strDate) {
        DateFormat format = new SimpleDateFormat(ContractDetailConsts.DATE_YYYYMMDD);
        format.setLenient(false);
        try {
            Integer.parseInt(strDate);
            format.parse(strDate);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /**
     * 保険金額区分変換
     *
     * @param hokenShuruiCD 保険種類コード
     * @return 保険金額区分変換結果
     * @throws Exception
     */
    private String getHokenKingakuKbn(String hokenShuruiCD) throws Exception {
        String result = ContractDetailConsts.BLANK;
        if (this.chkStringItemInList(hokenShuruiCD,
                        this.toListFromCommaString(ContractDetailConsts.HOKEN_TYPE_LIST_NENKIN))) {
            result = ContractDetailConsts.HOKEN_TYPE_NENKIN;
        } else if (this.chkStringItemInList(hokenShuruiCD,
                        this.toListFromCommaString(ContractDetailConsts.HOKEN_TYPE_LIST_NEN))) {
            result = ContractDetailConsts.HOKEN_TYPE_NEN;
        } else if (this.chkStringItemInList(hokenShuruiCD,
                        this.toListFromCommaString(ContractDetailConsts.HOKEN_TYPE_LIST_TSUKI))) {
            result = ContractDetailConsts.HOKEN_TYPE_TSUKI;
        } else if (this.chkStringItemInList(hokenShuruiCD,
                        this.toListFromCommaString(ContractDetailConsts.HOKEN_TYPE_LIST_NICHI))) {
            result = ContractDetailConsts.HOKEN_TYPE_NICHI;
        }
        return result;
    }

    /**
     * 指定文字列が配列に存在するかチェック
     *
     * @param item 検索対象文字列
     * @param list 検索元リスト
     * @return チェック結果 true:存在 false:なし
     * @exception  Exception 異常
     */
    private boolean chkStringItemInList(String item, String[] list) throws Exception {
        for (String str : list) {
            if (item.equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * コンマ区切りの文字列を配列にセットする
     *
     * @param str コンマ区切り文字列
     * @return 配列
     * @exception  Exception 異常
     */
    private String[] toListFromCommaString(String str) throws Exception {
        String[] list = str.split(",");
        return list;
    }

    /**
     * 文字列日付フォーマット
     *
     * @param date 文字列日付
     * @return yyyy/M/d文字列日付
     */
    private String stringFormatDate(String date) {
        if (StringUtils.isBlank(date)) {
            return ContractDetailConsts.BLANK;
        }
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);
        if (ContractDetailConsts.NUMBER_0.equals(month.substring(0, 1))) {
            month = month.substring(1, 2);
        }
        if (ContractDetailConsts.NUMBER_0.equals(day.substring(0, 1))) {
            day = day.substring(1, 2);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(year);
        sb.append(ContractDetailConsts.SLASH);
        sb.append(month);
        sb.append(ContractDetailConsts.SLASH);
        sb.append(day);
        return sb.toString();
    }

    /**
     * 指定された値を数値フォーマット化して返す。
     *
     * @param value    変換対象値
     * @return 数値形式の文字列
     * @exception  Exception 異常
     */
    private String numDecimalFormat(Object value) throws Exception {
        if (value == null || ContractDetailConsts.BLANK.equals(value)) {
            return ContractDetailConsts.BLANK;
        }
        NumberFormat nfNum = new DecimalFormat(ContractDetailConsts.DECIMAL_FORMAT);
        // 数値フォーマット化
        return nfNum.format(Double.valueOf(String.valueOf(value)));
    }

    /**
     * 金額を割り算した結果を返す。
     *
     * @param lngAmount 金額(Long)
     * @param wari      分布
     * @param decPoint  小数点
     * @param round     Round
     * @return 割り算結果
     * @exception  Exception 異常
     */
    private String getAmountDivide(Long lngAmount, int wari, int decPoint, int round) throws Exception {
        BigDecimal decAmount = new BigDecimal(lngAmount);
        BigDecimal dist = new BigDecimal(wari);
        BigDecimal divide = decAmount.divide(dist, decPoint, round);
        return String.valueOf(divide);
    }

    /**
     * 金額を割り算した結果を返す。
     *
     * @param decAmount 金額(BigDecimal)
     * @param wari      分布
     * @param decPoint  小数点
     * @param round     Round
     * @return 割り算結果
     * @exception  Exception 異常
     */
    private String getAmountDivide(BigDecimal decAmount, int wari, int decPoint, int round) throws Exception {
        BigDecimal dist = new BigDecimal(wari);
        BigDecimal divide = decAmount.divide(dist, decPoint, round);
        return String.valueOf(divide);
    }

    /**
     * 指定された値のフォーマットを数値形式に変換して返す。
     *
     * @param value    変換対象値
     * @return 数値形式の文字列
     * @exception  Exception 異常
     */
    private String numFormat(Object value) throws Exception {
        if (value == null || ContractDetailConsts.BLANK.equals(value)) {
            return ContractDetailConsts.BLANK;
        }
        NumberFormat nfNum = NumberFormat.getNumberInstance();
        // 数値形式の文字列を作成
        return nfNum.format(Double.valueOf(String.valueOf(value)));
    }

    /**
     * 最終結果コードを元に最終結果の名称を返す。
     *
     * @param resultCD 最終結果コード
     * @param seiritsuKakuteibi 成立確定日
     * @param fuseiritsuKakuteibi 不成立確定日
     * @return 最終結果名称
     * @exception  Exception 異常
     */
    private String getFinalResultName(String resultCD, String seiritsuKakuteibi,
                    String fuseiritsuKakuteibi) throws Exception {
        // 以下の条件に当てはまらない最終結果は、未成立とする。
        String resultMeisho = ContractDetailConsts.FINAL_RESULT_MISEIRITSU;
        if (ContractDetailConsts.DEFAULT_DATE.compareTo(seiritsuKakuteibi) != 0) {
            // 成立確定日が初期値以外の場合
            resultMeisho = ContractDetailConsts.FINAL_RESULT_SEIRITSU;
        } else {
            // 成立確定日が初期値の場合
            if (ContractDetailConsts.DEFAULT_DATE.compareTo(fuseiritsuKakuteibi) != 0) {
                // 不成立確定日が初期値以外の場合
                if (ContractDetailConsts.FINAL_RESULT_CD_I.equals(resultCD)
                                || ContractDetailConsts.FINAL_RESULT_CD_N.equals(resultCD)) {
                    // 申込管理．最終結果の上１桁が"I" or "N"の場合
                    resultMeisho = ContractDetailConsts.FINAL_RESULT_FUSEIRITSU;
                } else if (ContractDetailConsts.FINAL_RESULT_CD_R.equals(resultCD)) {
                    // 申込管理．最終結果の上１桁が"R"の場合
                    resultMeisho = ContractDetailConsts.FINAL_RESULT_SYASETSU;
                } else {
                    // 以外の場合
                    resultMeisho = ContractDetailConsts.BLANK;
                }
            }
        }
        return resultMeisho;
    }

}
