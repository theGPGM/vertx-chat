package org.george.mahjong.uitl;

import java.awt.image.ImageProducer;
import java.util.*;

/**
 * 0 - 8 万
 * 9 - 17 饼
 * 18 - 26 条
 * 27 - 33 东西南北中发白
 *
 * 有些番种需要额外的信息，这时候需要传入一个参数 param，这是一个 map，记载了以下信息：
 * isLastCard
 * isZiMo
 * isGangMoPai
 * isQiangGanghu
 * isHuJueZhang
 * menFeng
 * quanFeng
 * huCard
 * flowerCount
 */

public class MahJongUtils {

    public static boolean commonHu(int[] handCards){

        int count = 0;
        for(int k : handCards){
            count += k;
        }

        if(count % 3 != 2){
            return false;
        }

        for(int i = 0; i < 34; i++){
            // 有将牌
            if(handCards[i] >= 2){
                handCards[i] -= 2;
                if(hu(handCards)){
                    return true;
                }
                handCards[i] += 2;
            }
        }
        return false;
    }

    /**
     * 回溯法查找
     * @param pai
     * @return
     */
    private static boolean hu(int[] pai){

        boolean flag = true;
        for(int i = 0; i < pai.length; i++){
            if(pai[i] != 0){
                flag =  false;
            }
        }
        if(flag){
            return true;
        }

        // 4 张只能作为暗刻 + 顺子
        for(int i = 0; i < 34; i++){
            if(pai[i] == 4){
                pai[i] -= 3;
                if(hu(pai)){
                    return true;
                }
                pai[i] += 3;
            }
        }

        // 去掉刻子能不能胡
        for(int i = 0; i < 34; i++){
            if(pai[i] >= 3){
                pai[i] -= 3;
                if(hu(pai)){
                    return true;
                }
                pai[i] += 3;
            }
        }

        // 判顺子
        for(int i = 0; i <= 24; i++){
            if(i % 9 <= 6 && pai[i] >= 1 && pai[i + 1] >= 1 && pai[i + 2] >= 1){
                pai[i]--;
                pai[i + 1]--;
                pai[i + 2]--;

                if(hu(pai)){
                    return true;
                }

                pai[i]++;
                pai[i + 1]++;
                pai[i + 2]++;
            }
        }
        return false;
    }

    //================================== 88 番 ================================================

    /**
     * 大四喜
     * @param h
     * @return
     */
    public static boolean isDaSiXi(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        int count = 0;
        int[] n = new int[]{27,28,29,30};

        // 风碰
        for(int k : n){
            for(int j : peng){
                if (k == j) {
                    count++;
                }
            }
        }

        // 风明杠
        for(int k : n){
            for(int j : mingGang){
                if(k == j){
                    count++;
                }
            }
        }

        // 风暗杠
        for(int k : n){
            for(int j : anGang){
                if(k == j){
                    count++;
                }
            }
        }

        // 遗漏的暗刻
        for(int k : n){
            if(h[k] >= 3){
                count++;
            }
        }
        return count == 4;
    }

    /**
     * 大三元
     * @param h
     * @return
     */
    public static boolean isDaSanYuan(int[] h, Map<String, Object> param){
        int count = 0;
        int[] n = new int[]{31,32,33};

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        // 碰
        for(int k : n){
            for(int j : peng){
                if (k == j) {
                    count++;
                }
            }
        }

        // 明杠
        for(int k : n){
            for(int j : mingGang){
                if(k == j){
                    count++;
                }
            }
        }

        // 暗杠
        for(int k : n){
            for(int j : anGang){
                if(k == j){
                    count++;
                }
            }
        }

        // 遗漏的暗刻
        for(int k : n){
            if(h[k] >= 3){
                count++;
            }
        }
        return count == 3;
    }

    /**
     * 绿一色
     * @param h
     * @return
     */
    public static boolean isLvYiSe(int[] h, Map<String, Object> param){
        int[] pai = h.clone();

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(int k : peng){
            pai[k] += 3;
        }

        if(mingGang.size() > 0){
            return false;
        }

        if(anGang.size() > 0){
            return false;
        }

        for(List<Integer> c : chi){
            pai[c.get(0)]++;
            pai[c.get(1)]++;
            pai[c.get(2)]++;
        }

        int[] n = new int[]{19 ,20 ,21 ,23 ,24 ,25, 32};
        for(int k : n){
            pai[k] = 0;
        }

        for(int k : pai){
            if(k != 0) return false;
        }

        return true;
    }

    /**
     * 九莲宝灯，如果要严格要求九莲宝灯，需要判断最后放入的牌不为 1、9
     * @param h
     * @return
     */
    public static boolean isJiuLianBaoDeng(int[] h, Map<String, Object> param){

        // 非清一色
        if(!isQingYiSe(h, param)){
            return false;
        }

        // 万
        if(h[0] != 0){
            if(h[0] < 3 || h[8] < 3){
                return false;
            }else{
                for(int i = 1; i < 8; i++){
                    if(h[i] == 0){
                        return false;
                    }
                }
            }
        }else if(h[9] != 0){
            if(h[9] < 3 || h[17] < 3){
                return false;
            }else{
                for(int i = 10; i < 17; i++){
                    if(h[i] == 0){
                        return false;
                    }
                }
            }
        }else{
            if(h[18] < 3 || h[26] < 3){
                return  false;
            }else{
                for(int i = 19; i < 26; i++){
                    if(h[i] == 0){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 连七对
     * @param h
     * @return
     */
    public static boolean isLianQiDui(int[] h, Map<String, Object> param){
        if(!isQingYiSe(h, param)){
            return false;
        }

        if(!isQiDui(h)){
            return false;
        }

        // 找到起始牌
        int i;
        for(i = 0; i < 27; i++){
            if(h[i] != 0){
                break;
            }
        }

        for(int j = 0; j < 7; j++){
            if(h[j + i] == 0){
                return false;
            }
        }

        return true;
    }

    /**
     * 十三幺
     * @param h
     * @return
     */
    public static boolean is13Yao(int[] h){
        // 1、9 、东、西、南、北、中、发、白
        return h[0] * h[8] * h[9] * h[17] * h[18] * h[26] * h[27] * h[28] * h[29] * h[30] * h[31] * h[32] * h[33] == 2;
    }

    /**
     * 四杠
     * @param h
     * @return
     */
    public static boolean isSiGang(int[] h, Map<String, Object> param){

        int mingGangCount = (Integer) param.get("mingGang");
        int anGangCount = (Integer) param.get("anGang");
        return mingGangCount + anGangCount == 4;
    }

    //================================== 64 番 ================================================

    /**
     * 清幺九
     * @param h
     * @return
     */
    public static boolean isQingYaoJiu(int[] h, Map<String, Object> param){

        // 碰
        List<Integer> peng = (List<Integer>)param.get("peng");

        for(int k : peng){
            h[k] += 3;
        }

        // 将牌数量
        int jCount = 0;
        // 1、9刻子数量
        int kCount = 0;

        int [] n = new int[]{0, 8, 9, 16, 17, 26};
        for(int k : n){
            if(h[k] == 3){
                kCount++;
            }else if(h[k] == 2){
                jCount++;
            }
        }
        return kCount == 4 && jCount == 1;
    }

    /**
     * 小四喜
     * @param h
     * @return
     */
    public static boolean isXiaoSiXi(int[] h, Map<String, Object> param){

        int[] n = new int[]{27, 28, 29, 30};

        int jCount = 0;
        int kCount = 0;

        // 碰
        List<Integer> peng = (List<Integer>)param.get("peng");
        for(int k : peng){
            for(int j : n){
                if(k == j){
                    kCount ++;
                }
            }
        }

        // 暗刻
        for(int k : n){
            if(h[k] >= 3){
                kCount++;
            }else if(h[k] == 2){
                jCount++;
            }
        }

        return kCount == 3 && jCount == 1;
    }

    /**
     * 小三元
     * @param h
     * @return
     */
    public static boolean isXiaoSanYuan(int[] h, Map<String, Object> param){
        int jCount = 0;
        int kCount = 0;
        int[] n = new int[]{31,32,33};

        // 碰
        List<Integer> peng = (List<Integer>)param.get("peng");
        for(int k : peng){
            for(int j : n){
                if(k == j){
                    kCount ++;
                }
            }
        }

        // 暗刻
        for(int k : n){
            if(h[k] >= 3){
                kCount++;
            }else if(h[k] == 2){
                jCount++;
            }
        }

        return kCount == 2 && jCount == 1;
    }

    /**
     * 字一色
     * @param h
     * @return
     */
    public static boolean isZiYiSe(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        int kCount = 0;
        int jCount = 0;

        int[] n = new int[]{27,28,29,30,31,32,33};
        for(int k : peng){
            for(int j : n){
                if(k == j)kCount++;
            }
        }

        for(int k : mingGang){
            for(int j : n){
                if(k == j)kCount++;
            }
        }

        for(int k : anGang){
            for(int j : n){
                if(k == j)kCount++;
            }
        }

        // 需要暗刻
        for(int k : n){
            if(h[k] >= 3){
                kCount++;
            }else if(h[k] == 2){
                jCount++;
            }
        }
        return kCount == 4 && jCount == 1;
    }

    /**
     * 四暗刻
     * @param h
     * @param param 参数中需要有 anGang : 表示暗杠数
     * @return
     */
    public static boolean isSiAnKe(int[] h, Map<String, Object> param){

        // 暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        int count = 0;
        for(int k : h){
            if(k >= 3){
                count++;
            }
        }
        return  count + anGang.size() == 4;
    }

    /**
     * 一色双龙会
     * @param h
     * @return
     */
    public static boolean isYiSeShuangLongHui(int[] h, Map<String, Object> param){

        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        if(!isQingYiSe(h, param)){
            return false;
        }

        for(int i = 0; i < 27; i++){
            if(h[i] > 0){
                if(h[i] != 2){
                    return false;
                }else if(i % 9 == 3 || i % 9 == 5){
                    return false;
                }
            }
        }
        return true;
    }

    //================================== 48 番 ================================================

    /**
     * 一色四同顺
     * @param h
     * @return
     */
    public static boolean isYiSeSiTongShun(int[] h, Map<String, Object> param){

        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int i;
        for(i = 0; i < 27; i++){
            if(h[i] == 4){
                break;
            }
        }

        // 超过了可取范围
        if(i % 9 >= 7){
            return false;
        }

        return h[i] == 4 && h[i + 1] == 4 && h[i + 2] == 4;
    }

    /**
     * 一色四节高
     * @param h
     * @return
     */
    public static boolean isYiSeSiJieGao(int[] h, Map<String, Object> param){

        List<Integer> peng = ((List<Integer>) param.get("peng"));

        for(int k : peng){
            h[k] += 3;
        }

        int i;
        for(i = 0; i < 27; i++){
            if(h[i] == 3){
                break;
            }
        }

        // 超过了可取范围
        if(i % 9 >= 6){
            return false;
        }

        return h[i] == 3 && h[i + 1] == 3 && h[i + 2] == 3 && h[i + 3] == 3;
    }

    //================================== 32 番 ================================================

    /**
     * 一色四步高
     * @param h
     * @return
     */
    public static boolean isYiSeSiBuGao(int[] h, Map<String, Object> param){

        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int count = 0;
        for(int k : h){
            count += k;
        }
        // 手牌需要十四张
        if(count < 14){
            return false;
        }

        int l = 0;
        int r = 26;
        while(l <= 26){
            if(h[l] == 1){
                break;
            }
            l++;
        }

        while(r >= 0){
            if(h[r] == 1){
                break;
            }
            r--;
        }

        if(r % 9 - l % 9 == 5){
            return h[l] == 1 && h[l + 1] == 2 && h[l + 2] == 3 && h[r - 2] == 3 && h[r - 1] == 2 && h[r] == 1;
        }

        if(r % 9 - l % 9 == 8){
            return h[l] == 1 && h[l + 1] == 1 && h[l + 2] == 2 && h[l + 3] == 1 &&  h[l + 4] == 2 && h[r - 3] == 1 && h[r - 2] == 2 && h[r - 1] == 1 && h[r] == 1;
        }

        return false;
    }

    /**
     * 三杠
     * @return
     */
    public static boolean isSanGang(Map<String, Object> param){

        int mingGang = (Integer) param.get("mingGang");
        int anGang = (Integer) param.get("anGang");
        return  mingGang + anGang == 3;
    }

    /**
     * 混幺九
     * @param h
     * @return
     */
    public static boolean isHunYaoJiu(int[] h, Map<String, Object> param){

        int jCount = 0;
        int kCount = 0;

        int[] n = new int[]{0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33};

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        for(int k : peng){
            for(int j : n){
                if(k == j) kCount++;
            }
        }

        // 暗刻
        for(int k : n){
            if(h[k] >= 3){
                kCount++;
            }else if(h[k] == 2 ){
                jCount ++;
            }
        }

        return kCount == 4 && jCount == 1;
    }

    //================================== 24 番 ================================================

    /**
     * 七对
     * @param h
     * @return
     */
    public static boolean isQiDui(int[] h){
        int pairCount = 0;
        for(int k : h){
            if(k == 0){
                continue;
            }else if(k == 2){
                pairCount++;
            }else if(k == 4){
                pairCount += 2;
            }else{
                return false;
            }
        }

        if(pairCount == 7){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 七星不靠
     * @param h
     * @return
     */
    public static boolean isQiXingBuKao(int[] h){

        // 存放万、饼、条
        List<List<Integer>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());

        if(h[27] * h[28] * h[29] * h[30] * h[31] * h[32] * h[33] == 1){

            for(int i = 0; i <= 26; i++){
                if(h[i] > 0){
                    if(i < 9){
                        list.get(0).add(i % 9);
                    }else if(i < 18){
                        list.get(1).add(i % 9);
                    }else{
                        list.get(2).add(i % 9);
                    }
                }
            }

            // 缺门
            for(int i = 0; i < 3; i++){
                if(list.get(i).size() == 0){
                    return false;
                }
            }

            // 判断万、条、饼内部是否满足隔 3 和 隔 6
            for(int i = 0; i < 3; i++){
                for(int j = 1; j < list.get(i).size(); j++){
                    if(list.get(i).get(j - 1) + 3 == list.get(i).get(j) || list.get(i).get(j - 1) + 6 == list.get(i).get(j)){
                        continue;
                    }else{
                        return false;
                    }
                }
            }

            // 万、饼、条对3取模是否独立
            int[] c = new int[3];
            for(int i = 0; i < 3; i++){
                c[list.get(i).get(0) % 3]++;
            }

            for(int i = 0; i < 3; i++){
                if(c[i] == 0){
                    return false;
                }
            }

            return true;
        }else{
            return false;
        }
    }

    /**
     * 清一色
     * @param h
     * @return
     */
    public static boolean isQingYiSe(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        // 存放万、饼、条的个数
        int mCount = 0;
        int bCount = 0;
        int sCount = 0;

        for(int i = 0; i <= 26; i++) {
            if (h[i] > 0) {
                if (i < 9) {
                    mCount += h[i];
                } else if (i < 18) {
                    bCount += h[i];
                } else {
                    sCount += h[i];
                }
            }
        }

        if(mCount == 14 || bCount == 14 || sCount == 14){
            return true;
        }
        return false;
    }

    /**
     * 全双刻
     * @param h
     * @return
     */
    public static boolean isQuanShuangKe(int[] h, Map<String, Object> param){

        int jCount = 0;
        int kCount = 0;

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        for(int k : peng){
            if(k % 9 == 1 || k % 9 == 3 || k % 9 == 5 || k % 9 == 7){
                kCount++;
            }
        }

        // 暗刻
        for(int i = 0; i < 27; i++){
            if(i % 9 == 1 || i % 9 == 3 || i % 9 == 5 || i % 9 == 7){
                if(h[i] == 2){
                    jCount++;
                }else if(h[i] >= 3){
                    kCount++;
                }
            }
        }

        return kCount == 4 && jCount == 1;
    }

    /**
     * 一色三同顺
     * @param h
     * @return
     */
    public static boolean isYiSeSanTongShun(int[] h, Map<String, Object> param){

        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 27; i++){
            if(i % 9 < (i + 1) % 9 && (i + 1) % 9 < (i + 2) % 9){
                if(h[i] == 3 && h[i + 1] == 3 && h[i + 2] == 3) return true;
            }
        }

        return false;
    }

    /**
     * 一色三节高
     * @param h
     * @param param
     * @return
     */
    public static boolean isYiSeSanJieGao(int[] h, Map<String, Object> param){
        List<Integer> peng = ((List<Integer>) param.get("peng"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int i = 0; i < 27; i++){
            if(i % 9 < (i + 1) % 9 && (i + 1) % 9 < (i + 2) % 9){
                if(h[i] == 3 && h[i + 1] == 3 && h[i + 2] == 3) return true;
            }
        }

        return false;
    }

    /**
     * 全大
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanDa(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 27; i++){
            if(i % 9 < 6 && h[i] != 0){
                return false;
            }
        }

        for(int i = 27; i < 34; i++){
            if(h[i] != 0)  return false;
        }

        return true;
    }

    /**
     * 全中
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanZhong(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 27; i++){
            if((i % 9 < 3 || i % 9 > 5) && h[i] != 0){
                return false;
            }
        }

        for(int i = 27; i < 34; i++){
            if(h[i] != 0)  return false;
        }

        return true;
    }

    /**
     * 全小
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanXiao(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 27; i++){
            if((i % 9 > 2) && h[i] != 0){
                return false;
            }
        }

        for(int i = 27; i < 34; i++){
            if(h[i] != 0)  return false;
        }

        return true;
    }

    //================================== 16 番 ================================================

    /**
     * 清龙
     * @param h
     * @param param
     * @return
     */
    public static boolean isQingLong(int[]h, Map<String, Object> param){

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        boolean f1 = true;
        boolean f2 = true;
        boolean f3 = true;

        for(int i = 1; i < 9; i++){
            if(h[i - 1] != h[i] || h[i] != 1){
                f1 = false;
                break;
            }
        }

        for(int i = 10; i < 18; i++){
            if(h[i - 1] != h[i] || h[i] != 1){
                f2 = false;
                break;
            }
        }

        for(int i = 19; i < 27; i++){
            if(h[i - 1] != h[i] || h[i] != 1){
                f3 = false;
                break;
            }
        }

        return f1 || f2 || f3;
    }

    /**
     * 三色双龙会
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanSeSangLongHui(int[] h, Map<String, Object> param ){
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        boolean f1 = false;
        boolean f2 = true;
        boolean f3 = true;

        for(int i = 0; i < 9; i++){
            if(h[i] != 0){
                f1 = true;
                break;
            }
        }

        for (int i = 9; i < 18; i++){
            if(h[i] != 0){
                f2 = true;
                break;
            }
        }

        for(int i = 18; i < 27; i++){
            if(h[i] != 0){
                f3 = true;
                break;
            }
        }

        if(f1 && f2 && f3){
            // 缺门
            return false;
        }

        if(h[4] != 2 && h[13] != 2 && h[22] != 2){
            return false;
        }

        if(h[0] != 1){
            for(int i = 0; i < 3; i++){
                if(h[i + 9] != 1) return false;
                if(h[i + 18] != 1) return false;
            }
            for(int i = 7; i < 9; i++){
                if(h[i + 9] != 1) return false;
                if(h[i + 18] != 1) return false;
            }
        }else if(h[9] != 1){
            for(int i = 0; i < 3; i++){
                if(h[i] != 1) return false;
                if(h[i + 18] != 1) return false;
            }
            for(int i = 7; i < 9; i++){
                if(h[i] != 1) return false;
                if(h[i + 18] != 1) return false;
            }
        }else if(h[18] != 1){
            for(int i = 0; i < 3; i++){
                if(h[i] != 1) return false;
                if(h[i + 9] != 1) return false;
            }
            for(int i = 7; i < 9; i++){
                if(h[i] != 1) return false;
                if(h[i + 9] != 1) return false;
            }
        }else{
            return false;
        }
        return true;
    }

    /**
     * 一色三步高
     * @param h
     * @param param
     * @return
     */
    public static boolean isYiSeSanBuGao(int[] h, Map<String, Object> param){

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int[] m = new int[9];
        int[] b = new int[9];
        int[] s = new int[9];

        for(int i = 0; i < 26; i++){
            if(i < 9){
                m[i] = h[i];
            }else if(i < 18){
                b[i % 9] = h[i];
            }else{
                s[i % 9] = h[i];
            }
        }

        int count = 0;
        for(int i = 0; i < 27; i++){
            if(h[i] == 2){
                if((i + 1)% 9 > i % 9 && (i - 1) % 9 < i % 9){
                    if(h[i - 1] == 1 && h[i + 1] == 1) {
                        if(++count == 2){
                            return true;
                        }
                    }
                }
            }else if(h[i] == 3){
                if((i + 1)% 9 > i % 9 && (i - 1) % 9 < i % 9){
                    if(h[i - 1] == 2 && h[i + 1] == 2) return true;
                }
            }
        }

        return false;
    }

    public static boolean isQuanDaiWu(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        int count = 0;

        for(int k : peng){
            if(k % 9 == 4){
                count ++;
            }
        }

        for(int k : mingGang){
            if(k % 9 == 4){
                count ++;
            }
        }

        for(int k : anGang){
            if(k % 9 == 4){
                count ++;
            }
        }

        for(List<Integer> list : chi){
            for(int k : list){
                if(k % 9 == 4){
                    count ++;
                }
            }
        }

        for(int i = 0; i < 3; i++){
            if(h[4 + i * 9] == 4){
                count += 2;
            }else if(h[4 + i * 9] == 3){
                count += 2;
            }else if(h[4 + i * 9] == 2){
                count ++;
            }else if(h[4 + i * 9] == 1){
                count++;
            }
        }

        return count == 4;
    }

    /**
     * 三同刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanTongKe(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(int i = 0; i < 9; i++){
            if(h[i] >= 3 && h[i + 9] >= 3 && h[i + 18] >= 3){
                return true;
            }
        }

        return false;
    }

    /**
     * 三暗刻
     * @param h
     * @return
     */
    public static boolean isSanAnKe(int[]h){
        int count = 0;
        for(int k : h){
            if(k >= 3){
                count ++;
            }
        }
        return count == 3;
    }

    //================================== 12 番 ================================================

    /**
     * 全不靠
     * @param h
     * @return
     */
    public static boolean isQuanBuKao(int[] h){

        // 存放万、饼、条
        List<List<Integer>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());
        list.add(new ArrayList<>());

        for(int i = 0; i <= 26; i++){
            if(h[i] > 0){
                if(i < 9){
                    list.get(0).add(i % 9);
                }else if(i < 18){
                    list.get(1).add(i % 9);
                }else{
                    list.get(2).add(i % 9);
                }
            }
        }

        // 缺门
        for(int i = 0; i < 3; i++){
            if(list.get(i).size() == 0){
                return false;
            }
        }

        // 判断万、条、饼内部是否满足隔 3 和 隔 6
        for(int i = 0; i < 3; i++){
            for(int j = 1; j < list.get(i).size(); j++){
                if(list.get(i).get(j - 1) + 3 == list.get(i).get(j) || list.get(i).get(j - 1) + 6 == list.get(i).get(j)){
                    continue;
                }else{
                    return false;
                }
            }
        }

        // 万、饼、条对3取模
        int[] c = new int[3];
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < list.get(i).size(); j++){
                c[list.get(i).get(j) % 3]++;
            }
        }

        for(int i = 0; i < 3; i++){
            if(c[i] != 3){
                return false;
            }
        }

        int count = 0;
        for(int i = 27; i <= 33; i++){
            if(h[i] > 1){
                return false;
            }else if(h[i] == 1){
                count++;
            }
        }
        return count == 5;
    }

    /**
     * 小于五
     * @param h
     * @param param
     * @return
     */
    public static boolean isDaYuWu(int[]h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            for(int k : list){
                h[k]++;
            }
        }

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] != 0){
                    return false;
                }
            }
        }

        for(int i = 27; i < 34; i++){
            if(h[i] != 0){
                return false;
            }
        }

        return true;
    }

    /**
     * 小于五
     * @param h
     * @param param
     * @return
     */
    public static boolean isXiaoYuWu(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            for(int k : list){
                h[k]++;
            }
        }

        for(int i = 5; i < 9; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] != 0){
                    return false;
                }
            }
        }

        for(int i = 27; i < 34; i++){
            if(h[i] != 0){
                return false;
            }
        }

        return true;
    }

    /**
     * 三风刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanFengKe(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        for(int k : peng){
            h[k] += 3;
        }

        int count = 0;
        for(int i = 27; i < 31; i++ ){
            if(h[i] == 3){
                count++;
            }
        }
        return count == 3;
    }

    //================================== 8 番  ================================================

    /**
     * 花龙
     * @param h
     * @param param
     * @return
     */
    public static boolean isHuaLong(int[] h, Map<String, Object> param){
        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(List<Integer> list : chi){
            for(int k : list){
                h[k]++;
            }
        }

        for(int i = 0; i < 9; i++){
            if(h[i] == 0 && h[i + 9] == 0 && h[i + 18] == 0){
                return false;
            }
        }

        int a = -1;
        int b = -1;
        int c = -1;

        // 先找出 1、2、3 在哪里
        for(int i = 0; i < 3; i++){
            if(h[0 + i * 9] != 0 && h[1 + i * 9] != 0 && h[2 + i * 9] != 0){
                a = i;
                break;
            }
        }

        // 再找出 4、5、6 在哪里
        for(int i = 0; i < 3; i++){
            if(h[3 + i * 9] != 0 && h[4 + i * 9] != 0 && h[5 + i * 9] != 0){
                b = i;
                break;
            }
        }

        // 再找出 7、8、9 在哪里
        for(int i = 0; i < 3; i++){
            if(h[6 + i * 9] != 0 && h[7 + i * 9] != 0 && h[8 + i * 9] != 0){
                c = i;
                break;
            }
        }

        // 没找到模式
        if (a == -1 || b == -1 || c == -1) {
            return false;
        }else{
            return a != b && b != c;
        }
    }

    /**
     * 推不倒
     * @param h
     * @param param
     * @return
     */
    public static boolean isTuiBuDao(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            for(int k : list){
                h[k]++;
            }
        }

        // 推不倒的所有情况
        int[] n = new int[]{9, 10, 11, 12, 13, 16, 17, 19, 21, 22, 23, 25, 26, 33};
        for(int k : n){
            h[k] = 0;
        }

        for(int k : h){
            if(k != 0) return false;
        }

        return true;
    }

    /**
     * 三色三同顺
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanSeSanTongShun(int[] h, Map<String, Object> param){

        // 取出吃
        List<List<Integer>> chi = (List<List<Integer>>) param.get("chi");

        for(List<Integer> list : chi){
            for(int k : list){
                h[k]++;
            }
        }

        int[] n = new int[9];
        for(int i = 0; i < 9; i++){
            if(h[i] != 0 && h[i + 9] != 0 && h[i + 18] != 0){
                n[i] = 1;
            }
        }

        for(int i = 0; i < 7; i++){
            if(n[i] != 0 && n[i + 1] != 0 && n[i + 2] != 0 ) return true;
        }

        return false;
    }

    /**
     * 三色三节高
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanSeSanJieGao(int[] h, Map<String, Object> param){
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        int[] n = new int[9];

        for(int k : peng){
            n[k % 9]++;
        }

        // 找出暗刻
        for(int i = 0; i < 27; i++){
            if(h[i] >= 3){
                n[i % 9]++;
            }
        }

        boolean flag = false;
        for(int i = 0; i < 7; i++){
            if(n[i] != 0 && n[i + 1] != 0 && n[i + 2] != 0 ){
                flag = true;
            }
        }

        if(!flag){
            return false;
        }

        for(int k : peng){
            h[k] = 3;
        }

        for(int i = 1; i < 8; i++){
            for(int j = 0; j < 3; j++){
                // 找到中间的刻
                if(h[i + j * 9] == 3){
                    // 找两边
                    if(h[(i - 1) + ((j + 1) % 3) * 9] == 3 && h[(i + 1) + ((j + 2) % 3) * 9] == 3){
                        return true;
                    }else if(h[(i - 1) + ((j + 2) % 3) * 9] == 3 && h[(i + 1) + ((j + 1) % 3) * 9] == 3){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * TODO
     * 无番和
     * @param h
     * @param param
     * @return
     */
    public static boolean isWuFanHe(int[] h, Map<String, Object> param){

        return false;
    }

    /**
     * 妙手回春
     * @param h
     * @param param
     * @return
     */
    public static boolean isMiaoShouHuiChun(int[] h, Map<String, Object> param){
        boolean isLastCard = (boolean) param.get("isLastCard");
        return isLastCard;
    }

    /**
     * 海底捞月
     * @param h
     * @param param
     * @return
     */
    public static boolean isHaiDiLaoYue(int[] h, Map<String, Object> param){
        boolean isLastCard = (boolean) param.get("isLastCard");
        boolean isZiMo = (boolean) param.get("isZiMo");
        return isZiMo && isLastCard;
    }

    /**
     * 杠上开花
     * @param h
     * @param param
     * @return
     */
    public static boolean isGangShangKaiHua(int[] h, Map<String, Object> param){
        boolean isGangMoPai = (boolean) param.get("isGangMoPai");
        return isGangMoPai;
    }

    /**
     * 抢杠和
     * @param h
     * @param param
     * @return
     */
    public static boolean isQiangGanghu(int[] h, Map<String, Object> param){
        boolean isQiangGangHu = (boolean) param.get("isQiangGangHu");
        return isQiangGangHu;
    }

    /**
     * 双暗杠
     * @param h
     * @param param
     * @return
     */
    public static boolean isShuangAnGang(int[] h, Map<String, Object> param){

        List<Integer> anGang = ((List<Integer>) param.get("anGang"));
        return anGang.size() == 2;
    }

    //================================== 6 番  ================================================

    /**
     * 碰碰和
     * @param h
     * @param param
     * @return
     */
    public static boolean isPengPengHu(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        int count = 0;
        count += peng.size();
        count += mingGang.size();
        count += anGang.size();

        // 找暗刻
        for(int k : h){
            if(k >= 3){
                count++;
            }
        }

        return count == 4;
    }

    /**
     * 混一色
     * 如果存在都是一种花色的，在检验清一色时就会被检验出来，这里只需要判断是否存在一种花色是 12 张即可
     * @param param
     * @return
     */
    public static boolean isHunYiSe(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        // 存放万、饼、条的个数
        int mCount = 0;
        int bCount = 0;
        int sCount = 0;

        for(int i = 0; i <= 26; i++) {
            if (h[i] > 0) {
                if (i < 9) {
                    mCount += h[i];
                } else if (i < 18) {
                    bCount += h[i];
                } else {
                    sCount += h[i];
                }
            }
        }

        if(mCount == 12 || bCount == 12 || sCount == 12){
            return true;
        }
        return false;
    }

    /**
     * 三色三步高
     * @param h
     * @param param
     * @return
     */
    public static boolean isSanSeSanBuGao(int[] h, Map<String, Object> param){

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        // 记录顺子出现的左边的牌的位置
        int[][] n = new int[3][9];

        for(int i = 0; i < 7; i ++ ){
            if(h[i] != 0 && h[i + 1] != 0 && h[i + 2] != 0){
                h[i]--;
                h[i + 1]--;
                h[i + 2]--;
                n[0][i] = 1;
            }
        }

        for(int i = 9; i < 16; i ++ ){
            if(h[i] != 0 && h[i + 1] != 0 && h[i + 2] != 0){
                h[i]--;
                h[i + 1]--;
                h[i + 2]--;
                n[1][i % 9] = 1;
            }
        }

        for(int i = 18; i < 25; i ++ ){
            if(h[i] != 0 && h[i + 1] != 0 && h[i + 2] != 0){
                h[i]--;
                h[i + 1]--;
                h[i + 2]--;
                n[2][i % 9] = 1;
            }
        }

        // 找中间的值
        for(int i = 2; i < 8; i++){
            for(int j = 0; j < 3; j++){
                if(n[j][i] != 0){
                    if(n[(j + 1) % 3][i - 1] != 0 && n[(j + 2) % 3][i + 1] != 0){
                        return true;
                    }
                    if(n[(j + 2) % 3][i - 1] != 0 && n[(j + 1) % 3][i + 1] != 0){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 五门齐
     * @param h
     * @param param
     * @return
     */
    public static boolean isWuMengQi(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int count = 0;
        for(int i = 0; i < 9; i++){
            if(h[i] != 0){
                count++;
                break;
            }
        }
        for(int i = 9; i < 18; i++){
            if(h[i] != 0){
                count++;
                break;
            }
        }
        for(int i = 18; i < 27; i++){
            if(h[i] != 0){
                count++;
                break;
            }
        }
        for(int i = 27; i < 31; i++){
            if(h[i] != 0){
                count++;
                break;
            }
        }
        for(int i = 31; i < 34; i++){
            if(h[i] != 0){
                count++;
                break;
            }
        }

        return count == 5;
    }

    /**
     * 全求人
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanQiuRen(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        int count = 0;
        count += peng.size();
        count += mingGang.size();
        count += chi.size();

        return count == 4;
    }

    /**
     * 双箭刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isShuangJianKe(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        int[] n = new int[]{31, 32, 33};
        int count = 0;
        for(int k : n){
            for(int j : peng){
                if(k == j){
                    count ++;
                }
            }
        }

        for(int k : n){
            if(h[k] >= 3){
                count++;
            }
        }

        return count == 2;
    }

    /**
     * 一明杠一暗杠
     * @param h
     * @param param
     * @return
     */
    public static boolean isYiMingGangYiAnGang(int[] h, Map<String, Object> param){

        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        return mingGang.size() == 1 && anGang.size() == 1;
    }

    //================================== 4 番  ================================================

    /**
     * 全带幺
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanDaiYao(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        // 做一下处理
        for(int i = 0; i < 3; i++){
            if(h[0 + (i * 9)] != 0){
                h[0 + (i * 9)] = 0;
                h[1 + (i * 9)] = 0;
                h[2 + (i * 9)] = 0;
            }
            if(h[8 + (i * 0)] != 0){
                h[6 + (i * 9)] = 0;
                h[7 + (i * 9)] = 0;
                h[8 + (i * 9)] = 0;
            }
        }

        int[] n = new int[]{27, 28, 29, 30, 31, 32, 33};

        for(int k : n){
            h[k] = 0;
        }

        for(int k : h){
            if(k != 0){
                return false;
            }
        }

        return true;
    }

    /**
     * 不求人
     * @param h
     * @param param
     * @return
     */
    public static boolean isBuQiuRen(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        return peng.size() == 0 && mingGang.size() == 0 && chi.size() == 0;
    }

    /**
     * 双明杠
     * @param h
     * @param param
     * @return
     */
    public static boolean isShuangMingGang(int[] h, Map<String, Object> param){

        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");

        return mingGang.size() == 2;
    }

    /**
     * 胡绝章
     * @param h
     * @param param
     * @return
     */
    public static boolean isHuJueZhang(int[] h, Map<String, Object> param){
        return (boolean)param.get("isHuJueZhang");
    }

    //================================== 2 番  ================================================

    /**
     * 箭刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isJianKe(int[] h, Map<String, Object> param){

        int[] n = new int[]{31, 32, 33};
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        for(int k : peng){
            for(int j : n){
                if(k == j) return true;
            }
        }

        for(int k : n){
            if(h[k] >= 3){
                return true;
            }
        }

        return false;
    }

    /**
     * 圈风刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isQuanFengKe(int[] h, Map<String, Object> param){

        int quanFeng = (Integer) param.get("quanFeng");
        for(int i = 27; i < 31; i++){
            if(h[i] == 3){
                return true;
            }
        }

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        for(int k : peng){
            for(int i = 27; i < 31; i++){
                if(i == k){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 门风刻
     * @param h
     * @param param
     * @return
     */
    public static boolean isMenFengKe(int[] h, Map<String, Object> param){

        int menFeng = (Integer) param.get("menFeng");
        for(int i = 27; i < 31; i++){
            if(h[i] == 3){
                return true;
            }
        }

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        for(int k : peng){
            for(int i = 27; i < 31; i++){
                if(i == k){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 门前清
     * @param h
     * @param param
     * @return
     */
    public static boolean isMenQianQing(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));
        // 是否自摸
        boolean isZiMo = (boolean)  param.get("isZiMo");

        return isZiMo && peng.size() == 0 && mingGang.size() == 0 && chi.size() == 0;
    }

    /**
     * 平胡
     * @param h
     * @param param
     * @return
     */
    public static boolean isPingHu(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("mingGang");

        if(peng.size() > 0 || mingGang.size() > 0 || anGang.size() > 0){
            return false;
        }

        for(int i = 27; i < 33; i++){
            if(h[i] != 0){
                return false;
            }
        }

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 27; i++){
            // 判断是 将牌 + 顺子还是刻
            if(h[i] == 3){
                h[i] = 0;
                if(hu(h)){
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 四归一
     * @param h
     * @param param
     * @return
     */
    public static boolean isSiGuiYi(int[] h, Map<String, Object> param){

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : h){
            if(k == 4){
                return true;
            }
        }

        return false;
    }

    public static boolean isShuangTongKe(int[] h, Map<String, Object> param){

        int[] n = new int[9];
        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");

        for(int k : peng){
            if(k >= 0 && k <= 26){
                n[k % 9]++;
            }
        }

        for(int i = 0; i < 27; i++){
            if(h[i] == 3){
                h[i] = 0;
                if(hu(h)){
                    n[i % 9]++;
                }
                h[i] = 3;
            }
            if(h[i] == 4){
                n[i % 9]++;
            }
        }

        for(int k : n){
            if(k == 2){
                return true;
            }
        }

        return false;
    }

    //================================== 1 番  ================================================

    public static void main(String[] args) {
        int [] pai = new int[34];

        // 牌型测试

        // 万
        pai[0] = 0;
        pai[1] = 0;
        pai[2] = 0;
        pai[3] = 0;
        pai[4] = 0;
        pai[5] = 0;
        pai[6] = 4;
        pai[7] = 1;
        pai[8] = 1;

        // 饼
        pai[9] = 0;
        pai[10] = 0;
        pai[11] = 0;
        pai[12] = 0;
        pai[13] = 0;
        pai[14] = 0;
        pai[15] = 0;
        pai[16] = 0;
        pai[17] = 0;

        // 条
        pai[18] = 0;
        pai[19] = 0;
        pai[20] = 0;
        pai[21] = 0;
        pai[22] = 0;
        pai[23] = 3;
        pai[24] = 0;
        pai[25] = 0;
        pai[26] = 0;

        // 东西南北中发白
        pai[27] = 0;
        pai[28] = 0;
        pai[29] = 0;
        pai[30] = 3;
        pai[31] = 1;
        pai[32] = 1;
        pai[33] = 1;

        Map<String, Object> map = new HashMap<>();
        List<Integer> peng = new ArrayList<>();
        List<Integer> mingGang = new ArrayList<>();
        List<Integer> angGang = new ArrayList<>();
        List<List<Integer>> chi = new ArrayList<>();

        map.put("chi", chi);
        map.put("peng", peng);
        map.put("mingGang", mingGang);
        map.put("angGang", angGang);
        System.out.println(isShuangTongKe(pai, map));
    }
}
