package com.example.pvdatafetch.task;

import com.example.pvdatafetch.mapper.DataMapper;
import com.yingfeng.api.IYFApi;
import com.yingfeng.api.YFFactory;
import com.yingfeng.api.YFNowval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author piiaJet
 * @Create 2025/4/818:45
 */
@Component
public class DataCollectTask {
    @Value("${api.host}")
    private String apiHost;

    @Value("${api.port}")
    private int apiPort;

    @Value("${api.username}")
    private String apiUsername;

    @Value("${api.password}")
    private String apiPassword;

    @Value("${company.table}")
    private String companyTable;

    @Autowired
    private DataMapper dataMapper;



    // 每5分钟执行一次（cron表达式示例）
    @Scheduled(fixedRate = 5*60*1000)
    public void collectAllTablesData() {
        // 步骤1：获取所有设备表
        List<String> deviceTables = dataMapper.listDeviceTablesFromCompanyTable(companyTable);
        Date alignTime = alignTimeToMinute(new Date());

        // 步骤2：处理每张表
        for (String tableName : deviceTables) {
            processSingleTable(tableName, alignTime);
        }
    }

    private void processSingleTable(String tableName, Date alignTime) {
        // 步骤2.1：获取表的字段列表（cpids）
        List<String> cpids = dataMapper.listTableFields(tableName);
        cpids = cpids.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        IYFApi connect = null;
        try {
            connect = YFFactory.CreateApi(apiHost, apiPort, apiUsername, apiPassword);

            // 步骤2.2：从外部系统获取实时值
            List<YFNowval> nowValues = connect.GetNowValue(cpids);
            System.out.println(nowValues);
            for (YFNowval nowval : nowValues) {
                if (nowval != null && nowval.value != null) {
                    System.out.println(nowval.Cpid+"\t"+nowval.value.Value);
                } else {
                    System.out.println("null or invalid value");
                }
            }

            // 步骤2.4：插入数据
            if (!nowValues.isEmpty()) {
                dataMapper.insertAlignedData(tableName, alignTime, nowValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭API连接
            if (connect != null) {
                try {
                    connect.Close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 时间对齐方法（取整分钟）

    private static Date alignTimeToMinute(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        // 将秒和毫秒归零
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // 获取当前分钟数
        int minute = cal.get(Calendar.MINUTE);

        // 对齐到最近的往前整5分钟
        int alignedMinute = (minute / 5) * 5;
        cal.set(Calendar.MINUTE, alignedMinute);

        return cal.getTime();
    }
}
