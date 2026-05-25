package com.example.financialmanagement.util;

import com.example.financialmanagement.model.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 生成文字版业务总结报告
 */
public class ReportHelper {

    public enum PeriodType {
        TODAY, WEEK, MONTH
    }

    /**
     * 生成报告文本
     *
     * @param transactions 交易记录列表（已按当前筛选条件过滤）
     * @param periodName   周期名称（如：2024-05-20、2024-05、2024）
     * @param periodType   周期类型
     * @param personName   筛选人名（"全部"则忽略）
     * @return 格式化报告文本
     */
    public static String generateReport(List<Transaction> transactions, String periodName,
                                         PeriodType periodType, String personName) {
        StringBuilder sb = new StringBuilder();

        // 标题
        String periodLabel;
        switch (periodType) {
            case TODAY:
                periodLabel = "日报";
                break;
            case WEEK:
                periodLabel = "周报";
                break;
            case MONTH:
                periodLabel = "月报";
                break;
            default:
                periodLabel = "报表";
        }

        sb.append("╔══════════════════╗\n");
        sb.append("║     记账系统 ").append(periodLabel).append("     ║\n");
        sb.append("╚══════════════════╝\n\n");

        sb.append("📅 统计周期：").append(periodName).append("\n");
        if (!"全部".equals(personName)) {
            sb.append("👤 筛选对象：").append(personName).append("\n");
        }
        sb.append("🕐 生成时间：").append(getCurrentTime()).append("\n\n");

        if (transactions == null || transactions.isEmpty()) {
            sb.append("━━━━━━━━━━━━━━\n");
            sb.append("该时段暂无记账记录。\n");
            sb.append("━━━━━━━━━━━━━━\n");
            return sb.toString();
        }

        // 汇总数据
        double totalIncome = 0;
        double totalExpense = 0;
        Map<String, Double> incomeByEvent = new HashMap<>();
        Map<String, Double> expenseByEvent = new HashMap<>();
        Map<String, Double> incomeByPerson = new HashMap<>();
        Map<String, Double> expenseByPerson = new HashMap<>();

        for (Transaction t : transactions) {
            double amount = t.getAmount();
            if (t.isIncome()) {
                totalIncome += amount;
                incomeByEvent.merge(t.getEvent(), amount, Double::sum);
                incomeByPerson.merge(t.getPerson(), amount, Double::sum);
            } else {
                totalExpense += amount;
                expenseByEvent.merge(t.getEvent(), amount, Double::sum);
                expenseByPerson.merge(t.getPerson(), amount, Double::sum);
            }
        }

        double balance = totalIncome - totalExpense;

        // 财务概览
        sb.append("━━━━━━━━━━━━━━\n");
        sb.append("【财务概览】\n");
        sb.append("━━━━━━━━━━━━━━\n");
        sb.append(String.format(Locale.getDefault(), "💰 总收入：%.0f 元\n", totalIncome));
        sb.append(String.format(Locale.getDefault(), "💸 总支出：%.0f 元\n", totalExpense));
        sb.append(String.format(Locale.getDefault(), "📊 结余：%.0f 元\n", balance));
        if (totalIncome > 0) {
            double ratio = totalExpense / totalIncome * 100;
            sb.append(String.format(Locale.getDefault(), "📈 支出占比：%.1f%%\n", ratio));
        }
        sb.append("\n");

        // 收入明细（按事件）
        if (!incomeByEvent.isEmpty()) {
            sb.append("━━━━━━━━━━━━━━\n");
            sb.append("【收入明细 - 按事件】\n");
            sb.append("━━━━━━━━━━━━━━\n");
            incomeByEvent.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> sb.append(String.format(Locale.getDefault(), "  + %-8s  %.0f 元\n", e.getKey(), e.getValue())));
            sb.append("\n");
        }

        // 支出明细（按事件）
        if (!expenseByEvent.isEmpty()) {
            sb.append("━━━━━━━━━━━━━━\n");
            sb.append("【支出明细 - 按事件】\n");
            sb.append("━━━━━━━━━━━━━━\n");
            expenseByEvent.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> sb.append(String.format(Locale.getDefault(), "  - %-8s  %.0f 元\n", e.getKey(), e.getValue())));
            sb.append("\n");
        }

        // 按人名统计（仅在未筛选单人时显示）
        if ("全部".equals(personName) && (incomeByPerson.size() > 1 || expenseByPerson.size() > 1)) {
            sb.append("━━━━━━━━━━━━━━\n");
            sb.append("【按人名统计】\n");
            sb.append("━━━━━━━━━━━━━━\n");

            Map<String, Double> personBalance = new HashMap<>();
            for (Map.Entry<String, Double> e : incomeByPerson.entrySet()) {
                personBalance.merge(e.getKey(), e.getValue(), Double::sum);
            }
            for (Map.Entry<String, Double> e : expenseByPerson.entrySet()) {
                personBalance.merge(e.getKey(), -e.getValue(), Double::sum);
            }

            personBalance.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> {
                        String sign = e.getValue() >= 0 ? "+" : "";
                        sb.append(String.format(Locale.getDefault(), "  %s：%s%.0f 元\n",
                                e.getKey(), sign, e.getValue()));
                    });
            sb.append("\n");
        }

        // 交易流水
        sb.append("━━━━━━━━━━━━━━\n");
        sb.append("【交易流水】\n");
        sb.append("━━━━━━━━━━━━━━\n");
        for (Transaction t : transactions) {
            String typeIcon = t.isIncome() ? "+" : "-";
            sb.append(String.format(Locale.getDefault(), "%s %s | %s | %s | %.0f 元\n",
                    typeIcon,
                    t.getDate(),
                    t.getPerson(),
                    t.getEvent(),
                    t.getAmount()));
        }

        sb.append("\n══════════════════\n");
        sb.append("     记账系统 · 数据报表     \n");
        sb.append("══════════════════\n");

        return sb.toString();
    }

    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * 根据日期字符串判断周期类型
     */
    public static PeriodType detectPeriodType(String dateStr) {
        if (dateStr == null) return PeriodType.MONTH;
        if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            // 判断是否为本周内
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(dateStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int week = cal.get(Calendar.WEEK_OF_YEAR);

                Calendar now = Calendar.getInstance();
                int nowWeek = now.get(Calendar.WEEK_OF_YEAR);

                if (week == nowWeek) return PeriodType.WEEK;
                return PeriodType.TODAY;
            } catch (Exception e) {
                return PeriodType.TODAY;
            }
        }
        if (dateStr.matches("\\d{4}-\\d{2}")) {
            return PeriodType.MONTH;
        }
        return PeriodType.MONTH;
    }
}
