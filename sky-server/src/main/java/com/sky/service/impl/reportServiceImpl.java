package com.sky.service.impl;

import com.aliyuncs.http.HttpResponse;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.service.reportService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTabJc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class reportServiceImpl implements reportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 营业额统计
     * @param begin
     * @param end
     */
    @Override
    public TurnoverReportVO turnoverStatisticcs(LocalDate begin, LocalDate end) {
        //1.先将日期封装好， 以 ， 进行分割
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        //让开始的日期加一，直到 = 结束日期
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        //封装成为字符串


        //2.获取到每一天的营业额
        //select sum(amount) from orders where order_time < ? and order_time > ? and status=5;
        List<Double> turnoverList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //获取这一天的开始时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //获取这一天的结束时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map<String,Object> map=new HashMap<>();
            map.put("beginTime",beginTime);
            map.put("endTime",endTime);
            map.put("status", Orders.COMPLETED);
            Double amount=orderMapper.getTurnoverStatistics(map);
            //没有营业额时，返回的是null
            amount= amount==null ? 0.0 :amount;
            turnoverList.add(amount);
        }

        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setTurnoverList(StringUtils.join(turnoverList,","));
        turnoverReportVO.setDateList(StringUtils.join(dateList ,","));

        return turnoverReportVO;


    }

    /**
     * 用户数据的统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //1.先将日期封装好， 以 ， 进行分割
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        //让开始的日期加一，直到 = 结束日期
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //2.查询新增用户
        //select count(id) from user where create_time> ? ang create_time<?;
        List<Integer> newUserList=new ArrayList<>();
        //总的用户数量
        //select count(id) from user where create_time < ?;
        List<Integer> totalUserList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //获取这一天的开始时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //获取这一天的结束时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer totalUser = userMapper.getNewUser(null, endTime);
            Integer count=userMapper.getNewUser(beginTime,endTime);
            newUserList.add(count);
            totalUserList.add(totalUser);
        }

        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList,","));
        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList,","));


        return userReportVO;
    }

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //先将日期封装好， 以 ， 进行分割
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        //让开始的日期加一，直到 = 结束日期
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);
        }

        //总订单数列表
        List<Integer> orderCountList=new ArrayList<>();
        //有效订单数列表
        List<Integer> validOrderCountList=new ArrayList<>();
        for (LocalDate date : dateList) {
            //获取这一天的开始时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            //获取这一天的结束时间
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //总订单数
            Integer orderCount = getOrderCount(beginTime,endTime,null);
            orderCountList.add(orderCount);
            //有效订单数
            Integer validCount = getOrderCount(beginTime,endTime,Orders.COMPLETED);
            validOrderCountList.add(validCount);

        }
        //有效订单数
        Integer completeCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //订单总数
        Integer totalOrder = orderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double  orderCompletionRate= 0.0;
        if(completeCount!=0){
            orderCompletionRate= completeCount.doubleValue() / totalOrder;
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCompletionRate(orderCompletionRate)
                .orderCountList(StringUtils.join(orderCountList,","))
                .totalOrderCount(totalOrder)
                .validOrderCount(completeCount)
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .build();
    }

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO saleTop10(LocalDate begin, LocalDate end) {
        //开始时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        //结束时间
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTO=orderMapper.getSaleTop10(beginTime,endTime);

        List<String> name = goodsSalesDTO.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = goodsSalesDTO.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(name,","))
                .numberList(StringUtils.join(number,","))
                .build();
    }

    /**
     * 导出excel表格
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        //1.得到模板的表格
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("templete/运营数据报表模板.xlsx");
        //得到开始时间和结束时间
        LocalDate dateBegin=LocalDate.now().minusDays(30);
        LocalDate dateEnd=LocalDate.now().minusDays(1);
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //写入时间数据
            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue("时间："+dateBegin+ "至"+dateEnd);
            BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));
            //写入概览数据
            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover()); //营业额
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate()); //订单完成率
            row.getCell(6).setCellValue(businessData.getNewUsers()); //新增用户数
            row=sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount()); //有效订单数
            row.getCell(4).setCellValue(businessData.getUnitPrice()); //平均客单价

            //写入明细数据
            for(int i=0;i<30;i++){
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                row=sheet.getRow(7+i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData1.getTurnover());
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData1.getUnitPrice());
                row.getCell(6).setCellValue(businessData1.getNewUsers());
            }

            //下载到客户端游览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status){
        Map<String,Object> map=new HashMap<>();
        map.put("beginTime",beginTime);
        map.put("endTime",endTime);
        map.put("status",status);
        Integer count = orderMapper.getOrderStatistics(map);
        return count;
    }
}
