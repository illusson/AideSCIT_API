package com.sgpublic.aidescit.api.module

import com.sgpublic.aidescit.api.core.spring.property.SemesterInfoProperty
import com.sgpublic.aidescit.api.core.util.Log
import com.sgpublic.aidescit.api.exceptions.ServerRuntimeException
import com.sgpublic.aidescit.api.mariadb.dao.ClassChartRepository
import com.sgpublic.aidescit.api.mariadb.dao.FacultyChartRepository
import com.sgpublic.aidescit.api.mariadb.dao.SpecialtyChartRepository
import com.sgpublic.aidescit.api.mariadb.dao.UserInfoRepository
import com.sgpublic.aidescit.api.mariadb.domain.ClassChart
import com.sgpublic.aidescit.api.mariadb.domain.FacultyChart
import com.sgpublic.aidescit.api.mariadb.domain.SpecialtyChart
import com.sgpublic.aidescit.api.mariadb.domain.UserInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.regex.Pattern

/**
 * 用户基本信息模块
 */
@Component
class UserInfoModule {
    @Autowired
    private lateinit var info: UserInfoRepository
    @Autowired
    private lateinit var classChart: ClassChartRepository
    @Autowired
    private lateinit var facultyChart: FacultyChartRepository
    @Autowired
    private lateinit var specialtyChart: SpecialtyChartRepository
    @Autowired
    private lateinit var session: SessionModule

    /**
     * 获取用户基本信息
     * @param username 用户学号/工号
     */
    fun get(username: String): UserInfo {
        val userInfo = info.getByUsername(username)
            ?: return refresh(username)
        return if (userInfo.isExpired()){
            refresh(username)
        } else {
            userInfo
        }
    }

    /**
     * 从教务系统刷新用户基本信息
     * @param username 用户学号/工号
     */
    private fun refresh(username: String): UserInfo {
        Log.d("刷新用户信息", username)
        val result = UserInfo()
        result.username = username
        val session = session.get(username).run {
            result.identify = identify
            return@run session
        }
        val url1 = "http://218.6.163.93:8081/xsgrxx.aspx?xh=$username"
        val doc1 = APIModule.executeDocument(
            url = url1,
            cookies = APIModule.buildCookies(
                APIModule.COOKIE_KEY to session
            ),
            headers = APIModule.buildHeaders(
                "Referer" to url1
            ),
            method = APIModule.METHOD_GET
        ).document
        result.grade = doc1.select("#lbl_dqszj").text().run {
            if (this == ""){
                throw ServerRuntimeException("年级获取失败")
            }
            return@run toShortOrNull() ?: throw ServerRuntimeException("年级ID解析失败")
        }
        result.name = doc1.select("#xm").text().run {
            if (this == ""){
                throw ServerRuntimeException("姓名获取失败")
            }
            return@run this
        }
        val lblXzb = doc1.select("#lbl_xzb").text().run {
            if (this == ""){
                throw ServerRuntimeException("班级名称获取失败")
            }
            return@run this
        }
        result.classId = lblXzb.run {
            if (this == ""){
                throw ServerRuntimeException("班级名称获取失败")
            }
            val match = Pattern.compile("(\\d+)\\.?(\\d+)班").matcher(this)
            if (match.find()){
                return@run match.group(0)
                    .replace("班", "").toShortOrNull()
                    ?: throw ServerRuntimeException("班级ID解析失败")
            } else {
                throw ServerRuntimeException("班级ID获取失败")
            }
        }
        val lblXy = doc1.select("#lbl_xy").text().run {
            if (this == ""){
                throw ServerRuntimeException("学院名称获取失败")
            }
            return@run this
        }

        val lblZymc = doc1.select("#lbl_zymc").text().run {
            if (this == ""){
                throw ServerRuntimeException("专业名称获取失败")
            }
            return@run this
        }
        val url2 = "http://218.6.163.93:8081/tjkbcx.aspx?xh=$username"
        val doc2 = APIModule.executeDocument(
            url = url2,
            cookies = APIModule.buildCookies(
                APIModule.COOKIE_KEY to session
            ),
            headers = APIModule.buildHeaders(
                "Referer" to url2
            ),
            method = APIModule.METHOD_GET
        )
        result.faculty = doc2.document.select("#xy").select("option").run {
            forEach { element ->
                if (element.text() == lblXy){
                    return@run element.attr("value").toIntOrNull()
                        ?: throw ServerRuntimeException("学院ID解析失败")
                }
            }
            throw ServerRuntimeException("学院ID获取失败：$lblXy")
        }
        val yearStart = SemesterInfoProperty.YEAR.split("-")[0].toInt()
        var viewstate = doc2.viewstate
        for (i in 0 until 6){
            val year = "${yearStart - i}-${yearStart - i + 1}"
            val doc3 = APIModule.executeDocument(
                url = url2,
                headers = APIModule.buildHeaders(
                    "Referer" to url2
                ),
                cookies = APIModule.buildCookies(
                    APIModule.COOKIE_KEY to session
                ),
                body = APIModule.buildFormBody(
                    "__EVENTTARGET" to "xq",
                    "__EVENTARGUMENT" to "",
                    "__LASTFOCUS" to "",
                    "__VIEWSTATE" to viewstate,
                    "__VIEWSTATEGENERATOR" to "3189F21D",
                    "xn" to year,
                    "xq" to 1,
                    "nj" to result.grade,
                    "xy" to result.faculty,
                ),
                method = APIModule.METHOD_POST
            )
            viewstate = doc3.viewstate
            doc3.document.select("#zy").select("option").forEach { element ->
                if (element.text() != lblZymc){
                    return@forEach
                }
                result.specialty = element.attr("value").toIntOrNull()
                    ?: throw ServerRuntimeException("专业ID解析失败")
                specialtyChart.save(SpecialtyChart().apply {
                    specialty = result.specialty
                    name = lblZymc
                    faculty = result.faculty
                })
                facultyChart.save(FacultyChart().apply {
                    name = lblXy
                    faculty = result.faculty
                })
                classChart.save(ClassChart().apply {
                    specialty = result.specialty
                    faculty = result.faculty
                    name = lblXzb
                    grade = result.grade
                    classId = result.classId
                })
                info.save(result)
                return result
            }
        }
        throw ServerRuntimeException("专业ID获取失败：$lblZymc")
    }
}