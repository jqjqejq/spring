package jp.co.pmacmobile.app.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jp.co.pmacmobile.common.annotation.ResponseResult;
import jp.co.pmacmobile.common.constant.Consts;
import jp.co.pmacmobile.common.constant.MessageConsts;
import jp.co.pmacmobile.common.exception.MobileException;
import jp.co.pmacmobile.domain.dto.GNBAC06InitDTO;
import jp.co.pmacmobile.domain.dto.GNBAC06SearchConDTO;
import jp.co.pmacmobile.domain.dto.SystemUserInfo;
import jp.co.pmacmobile.domain.entity.GNBAC06AgentEntity;
import jp.co.pmacmobile.domain.entity.GNBAC06LcEntity;
import jp.co.pmacmobile.domain.entity.GNBAC06SearchEntity;
import jp.co.pmacmobile.domain.service.GNBAC06Service;

/**
 * @author 71432393
 *
 */
@ResponseResult
@RestController
@RequestMapping("/search")
public class GNBAC06Controller {

    /**
     * セッションをインジェクションする
     */
    @Autowired
    HttpSession httpSession;

    /**
     * メッセージソースをインジェクションする
     */
    @Autowired
    MessageSource messageSource;

    /**
     * GNBAC06検索サービスをインジェクションする
     */
    @Autowired
    GNBAC06Service gnbac06Service;

    /**
     * GNBAC06検索初期化処理
     *
     * @return GNBAC06初期化DTO
     * @throws MobileException
     *
     */
    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public GNBAC06InitDTO init() throws Exception {

        // ユーザ情報取得
        SystemUserInfo userInfo = (SystemUserInfo) this.httpSession.getAttribute(Consts.USER_INFO);

        if (userInfo == null) {
            throw new MobileException("userinfo 404", "ユーザ情報が取得できません");
        }

        // ユーザ情報から支社コードを取得する
        String sectCode = userInfo.getSishaCode();

        int countSectCode = this.gnbac06Service.checkSectCode(sectCode);

        if (countSectCode == 0) {
            throw new MobileException(MessageConsts.W006.code(),
                            this.messageSource.getMessage(MessageConsts.SEARCH_COUNT.code(), new Object[] { 0 }, null));
        }

        // 支部営業所情報を取得する
        List<String> agentNoList = this.gnbac06Service.getAgentNo(sectCode);
        // 営業部情報リストを取得する
        List<GNBAC06AgentEntity> agentList = this.gnbac06Service.getAgentList(agentNoList);
        // 募集人リストを取得する
        List<GNBAC06LcEntity> bosyuList = this.gnbac06Service.getLcList(sectCode);

        GNBAC06InitDTO initDto = new GNBAC06InitDTO();
        initDto.setSisyaName(this.gnbac06Service.getSectName(sectCode));
        initDto.setAgentList(agentList);
        initDto.setBosyuList(bosyuList);

        return initDto;
    }

    /**
     * GNBAC06検索処理
     *
     * @param userInfo ユーザ情報
     * @return GNBAC06初期化DTO
     * @throws MobileException
     *
     */
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public @ResponseBody List<GNBAC06SearchEntity> search(@RequestBody GNBAC06SearchConDTO dto) throws MobileException {

        // ユーザ情報取得
        SystemUserInfo userInfo = (SystemUserInfo) this.httpSession.getAttribute(Consts.USER_INFO);

        if (userInfo == null) {
            throw new MobileException("userinfo 404", "ユーザ情報が取得できません");
        }

        dto.setUserSectCode(userInfo.getSishaCode());

        List<GNBAC06SearchEntity> resList = this.gnbac06Service.getSearchList(dto);
        return resList;
    }
}
