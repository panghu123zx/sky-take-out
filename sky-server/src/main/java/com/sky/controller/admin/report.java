package com.sky.controller.admin;

import com.aliyuncs.http.HttpResponse;
import com.sky.result.Result;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sky.service.reportService;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 数据统计相关接口
 */
@RestController
@Slf4j
@Api(tags = "数据统计相关接口")
@RequestMapping("/admin/report")
public class report {

    @Autowired
    private reportService  reportService;


    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> turnoverStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                       @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("营业额统计...");
        TurnoverReportVO turnoverReportVO = reportService.turnoverStatisticcs(begin, end);
        return Result.success(turnoverReportVO);
    }

    /**
     * 用户数据的统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("用户数据的统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> userStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                               @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("用户数据的统计...");
        UserReportVO userReportVO = reportService.userStatistics(begin,end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("订单统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> orderStatistics(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("订单统计...");
        OrderReportVO orderReportVO= reportService.ordersStatistics(begin,end);
        return Result.success(orderReportVO);
    }


    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    @ApiOperation("查询销量排名top10接口")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(@DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate begin,
                                            @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate end){
        log.info("查询销量排名top10接口...");
        SalesTop10ReportVO salesTop10ReportVO= reportService.saleTop10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 导出excel表格
     * @param response
     */
    @GetMapping("/export")
    @ApiOperation("导出excel表格")
    public  void export(HttpServletResponse response){
        reportService.export(response);
    }
}
