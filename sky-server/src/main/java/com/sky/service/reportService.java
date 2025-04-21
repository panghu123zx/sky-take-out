package com.sky.service;

import com.aliyuncs.http.HttpResponse;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface reportService {
    /**
     * 营业额统计
     * @param begin
     * @param end
     */
    TurnoverReportVO turnoverStatisticcs(LocalDate begin, LocalDate end);

    /**
     * 用户数据的统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO ordersStatistics(LocalDate begin, LocalDate end);

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO saleTop10(LocalDate begin, LocalDate end);

    /**
     * 导出excel表格
     * @param response
     */
    void export(HttpServletResponse response);
}
