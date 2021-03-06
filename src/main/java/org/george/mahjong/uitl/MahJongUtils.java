package org.george.mahjong.uitl;

import org.george.mahjong.cache.MahJongCache;
import org.george.mahjong.cache.bean.PlayerCacheBean;
import org.george.mahjong.cache.bean.RoomCacheBean;

import java.util.*;

/**
 * 0 - 8 万
 * 9 - 17 饼
 * 18 - 26 条
 * 27 - 33 东西南北中发白
 *
 * 有些番种需要额外的信息，这时候需要传入一个参数 param，这是一个 map，记载了以下信息：
 * isLastCard：是否是最后一张
 * isZiMo：是否自摸
 * isGangMoPai：是否是杠摸牌
 * isQiangGanghu：是否是抢杠胡
 * isHuJueZhang：是否是胡绝章
 * menFeng：门风
 * quanFeng：圈风
 * huCard：胡牌
 * flowerCount：花牌数
 * peng：副露中的碰
 * mingGang：副露中的明杠
 * angGang：副露中的暗杠
 * chi：副露中的吃
 */

public class MahJongUtils {

    private static boolean commonHu(int[] p){
        
        int[] pai = p.clone();

        int count = 0;
        for(int k : pai){
            count += k;
        }

        if(count % 3 != 2){
            return false;
        }

        for(int i = 0; i < 34; i++){
            // 有将牌
            if(pai[i] >= 2){
                pai[i] -= 2;
                if(hu(pai)){
                    return true;
                }
                pai[i] += 2;
            }
        }
        return false;
    }

    /**
     * 回溯法查找
     * 查找去掉将牌后 3*m 是否能胡
     * @param pai
     * @return
     */
    private static boolean hu(int[] pai){

        for(int k : pai){
            if(k < 0) return false;
        }

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
     * @param pai
     * @return
     */
    private static boolean isDaSiXi(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();
        
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
     * @param pai
     * @return
     */
    private static boolean isDaSanYuan(int[] pai, Map<String, Object> param){
        
        int[] h = pai.clone();
        
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
     * @param pai
     * @return
     */
    private static boolean isLvYiSe(int[] pai, Map<String, Object> param){
        
        int[] h = pai.clone();

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

        if(mingGang.size() > 0){
            return false;
        }

        if(anGang.size() > 0){
            return false;
        }

        for(List<Integer> c : chi){
            h[c.get(0)]++;
            h[c.get(1)]++;
            h[c.get(2)]++;
        }

        int[] n = new int[]{19 ,20 ,21 ,23 ,24 ,25, 32};
        for(int k : n){
            h[k] = 0;
        }

        for(int k : h){
            if(k != 0) return false;
        }

        return true;
    }

    /**
     * 九莲宝灯，如果要严格要求九莲宝灯，需要判断最后放入的牌不为 1、9
     * @param pai
     * @return
     */
    private static boolean isJiuLianBaoDeng(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();
        
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
     * @param pai
     * @return
     */
    private static boolean isLianQiDui(int[] pai, Map<String, Object> param){
        
        int[] h = pai.clone();
        
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
     * @param pai
     * @return
     */
    private static boolean is13Yao(int[] pai){
        int[] h = pai.clone();
        // 1、9 、东、西、南、北、中、发、白
        return h[0] * h[8] * h[9] * h[17] * h[18] * h[26] * h[27] * h[28] * h[29] * h[30] * h[31] * h[32] * h[33] == 2;
    }

    /**
     * 四杠
     * @param pai
     * @return
     */
    private static boolean isSiGang(int[] pai, Map<String, Object> param){

        List<Integer> ming = (List<Integer>) param.get("mingGang");
        List<Integer> an = (List<Integer>) param.get("anGang");
        return ming.size() + an.size() == 4;
    }

    //================================== 64 番 ================================================

    /**
     * 清幺九
     * @param pai
     * @return
     */
    private static boolean isQingYaoJiu(int[] pai, Map<String, Object> param){
        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isXiaoSiXi(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isXiaoSanYuan(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isZiYiSe(int[] pai, Map<String, Object> param){
        int[] h = pai.clone();

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
     * @param pai
     * @param param 参数中需要有 anGang : 表示暗杠数
     * @return
     */
    private static boolean isSiAnKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isYiSeShuangLongHui(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isYiSeSiTongShun(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isYiSeSiJieGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        List<Integer> peng = ((List<Integer>) param.get("peng"));
        List<Integer> mingGang = ((List<Integer>) param.get("mingGang"));
        List<Integer> anGang = ((List<Integer>) param.get("anGang"));

        for(int k : peng){
            h[k] += 3;
        }

        for(int k : mingGang){
            h[k] += 4;
        }

        for(int k : anGang){
            h[k] += 4;
        }

        int i;
        for(i = 0; i < 27; i++){
            if(h[i] >= 3){
                break;
            }
        }

        // 超过了可取范围
        if(i % 9 > 6){
            return false;
        }

        return h[i] >= 3 && h[i + 1] >= 3 && h[i + 2] >= 3 && h[i + 3] >= 3;
    }

    //================================== 32 番 ================================================

    /**
     * 一色四步高
     * @param pai
     * @return
     */
    private static boolean isYiSeSiBuGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
    private static boolean isSanGang(int[] pai, Map<String, Object> param){

        List<Integer> ming = (List<Integer>) param.get("mingGang");
        List<Integer> an = (List<Integer>) param.get("anGang");
        return ming.size() + an.size() == 3;
    }

    /**
     * 混幺九
     * @param pai
     * @return
     */
    private static boolean isHunYaoJiu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isQiDui(int[] pai){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isQiXingBuKao(int[] pai){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isQingYiSe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(int k : peng){
            if(k >= 27){
                return false;
            }
            h[k] += 3;
        }

        for(int k : mingGang){
            if(k >= 27){
                return false;
            }
            h[k] += 4;
        }

        for(int k : anGang){
            if(k >= 27){
                return false;
            }
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

        int count = 0;
        if(mCount != 0) count++;
        if(bCount != 0) count++;
        if(sCount != 0) count++;
        return count == 1;
    }

    /**
     * 全双刻
     * @param pai
     * @return
     */
    private static boolean isQuanShuangKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isYiSeSanTongShun(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isYiSeSanJieGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanDa(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanZhong(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanXiao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQingLong(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanSeSanLongHui(int[] pai, Map<String, Object> param ){
        
        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isYiSeSanBuGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

    /**
     * 全带五
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanDaiWu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanTongKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @return
     */
    private static boolean isSanAnKe(int[] pai){

        int[] h = pai.clone();

        int count = 0;
        for(int i = 0; i < 27; i++){
            if(h[i] == 3){
                h[i] -= 2;
                if(!hu(h)){
                    count++;
                }
                h[i] += 2;
            }else if(h[i] == 4){
                h[i] -= 3;
                if(hu(h)){
                    count++;
                }
                h[i] += 3;
            }
        }
        return count == 3;
    }

    //================================== 12 番 ================================================

    /**
     * 全不靠
     * @param pai
     * @return
     */
    private static boolean isQuanBuKao(int[] pai){

        int[] h = pai.clone();

        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] != 0 && h[i + 1 + j * 9] != 0){
                    return false;
                }
            }
        }

        int count = 0;
        for(int k : h){
            if(k != 0){
                count ++;
            }
        }

        return count == 14;
    }

    /**
     * 小于五
     * @param pai
     * @param param
     * @return
     */
    private static boolean isDaYuWu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isXiaoYuWu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanFengKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isHuaLong(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isTuiBuDao(int[] pai, Map<String, Object> param){
        
        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanSeSanTongShun(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanSeSanJieGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * 无番和
     * @param pai
     * @param param
     * @return
     */
    private static boolean isWuFanHe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int count = 0;

        int[] n = new int[]{27, 28, 29, 30, 31, 32, 33};

        List<Integer> peng = (List<Integer>) param.get("peng");
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        for(int k : n){
            for(int j : peng){
                if(j == k){
                    count++;
                }
            }
            for(int j : mingGang){
                if (j == k) {
                    count++;
                }
            }
            for(int j : anGang){
                if(j == k){
                    count++;
                }
            }
            if(h[k] != 0){
                count++;
            }
        }

        if(count != 1){
            return false;
        }
        return true;
    }

    /**
     * 妙手回春
     * @param pai
     * @param param
     * @return
     */
    private static boolean isMiaoShouHuiChun(int[] pai, Map<String, Object> param){
        boolean isLastCard = (boolean) param.get("isLastCard");
        return isLastCard;
    }

    /**
     * 海底捞月
     * @param pai
     * @param param
     * @return
     */
    private static boolean isHaiDiLaoYue(int[] pai, Map<String, Object> param){
        boolean isLastCard = (boolean) param.get("isLastCard");
        boolean isZiMo = (boolean) param.get("isZiMo");
        return isZiMo && isLastCard;
    }

    /**
     * 杠上开花
     * @param pai
     * @param param
     * @return
     */
    private static boolean isGangShangKaiHua(int[] pai, Map<String, Object> param){
        boolean isGangMoPai = (boolean) param.get("isGangMoPai");
        return isGangMoPai;
    }

    /**
     * 抢杠和
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQiangGanghu(int[] pai, Map<String, Object> param){
        boolean isQiangGangHu = (boolean) param.get("isQiangGangHu");
        return isQiangGangHu;
    }

    /**
     * 双暗杠
     * @param pai
     * @param param
     * @return
     */
    private static boolean isShuangAnGang(int[] pai, Map<String, Object> param){

        List<Integer> anGang = ((List<Integer>) param.get("anGang"));
        return anGang.size() == 2;
    }

    //================================== 6 番  ================================================

    /**
     * 碰碰和
     * @param pai
     * @param param
     * @return
     */
    private static boolean isPengPengHu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
    private static boolean isHunYiSe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

        int count = 0;
        if(mCount != 0)count ++;
        if(bCount != 0)count ++;
        if(sCount != 0)count ++;

        return count == 1;
    }

    /**
     * 三色三步高
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSanSeSanBuGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isWuMenQi(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanQiuRen(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        boolean isZiMo = (boolean)  param.get("isZiMo");

        int count = 0;
        count += peng.size();
        count += mingGang.size();
        count += chi.size();

        return count == 4 && isZiMo == false;
    }

    /**
     * 双箭刻
     * @param pai
     * @param param
     * @return
     */
    private static boolean isShuangJianKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isYiMingGangYiAnGang(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();
        
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        return mingGang.size() >= 1 && anGang.size() >= 1;
    }

    //================================== 4 番  ================================================

    /**
     * 全带幺
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanDaiYao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出碰
        List<Integer> peng = (List<Integer>) param.get("peng");
        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        // 取出暗杠
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            boolean flag = false;
            for(int k : list){
                if(k % 9 == 0 || k % 9 == 8){
                    flag = true;
                }
            }
            if(!flag){
                return false;
            }
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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isBuQiuRen(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isShuangMingGang(int[] pai, Map<String, Object> param){

        // 取出明杠
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");

        return mingGang.size() == 2;
    }

    /**
     * 胡绝章
     * @param pai
     * @param param
     * @return
     */
    private static boolean isHuJueZhang(int[] pai, Map<String, Object> param){
        return (boolean)param.get("isHuJueZhang");
    }

    //================================== 2 番  ================================================

    /**
     * 箭刻
     * @param pai
     * @param param
     * @return
     */
    private static boolean isJianKe(int[] pai, Map<String, Object> param){
        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQuanFengKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isMenFengKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isMenQianQing(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isPingHu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
     * @param pai
     * @param param
     * @return
     */
    private static boolean isSiGuiYi(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

    /**
     * 三同刻
     * @param pai
     * @param param
     * @return
     */
    private static boolean isShuangTongKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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
                h[i] -= 3;
                if(hu(h)) {
                    n[i % 9]++;
                }
                h[i] += 3;
            }
        }

        for(int k : n){
            if(k == 2){
                return true;
            }
        }

        return false;
    }

    /**
     * 双暗刻
     * 暗刻的情况有两种：
     * 1、三张相同的，暗刻
     * 2、四张相同的，暗刻 + 顺子
     *
     * 不是暗刻的情况:
     * 1、三张相同，将牌 + 顺子
     * 2、四张相同，将牌 + 顺子
     * @param pai
     * @param param
     * @return
     */
    private static boolean isShuangAnKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        int count = 0;
        for(int i = 0; i < 27; i++){
            if(h[i] == 3){
                // 将牌 + 顺子
                h[i] -= 2;
                if(hu(h)){
                    count++;
                }
                h[i] += 2;
            }else if(h[i] == 4){
                // 暗刻 + 顺子
                h[i] -= 3;
                if(hu(h)){
                    count++;
                }
                h[i] += 3;
            }
        }
        return count == 2;
    }

    /**
     * 暗杠
     * @param pai
     * @param param
     * @return
     */
    private static boolean isAnGang(int[] pai, Map<String, Object> param){
        List<Integer> anGang = (List<Integer>) param.get("anGang");
        return anGang.size() != 0;
    }

    /**
     * 断幺
     * @param pai
     * @param param
     * @return
     */
    private static boolean isDuanYao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

        int[] n = new int[]{0, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33};
        for(int k : n){
            if(h[k] != 0)return false;
        }

        return true;
    }

    //================================== 1 番  ================================================

    /**
     * 一般高
     * @param pai
     * @param param
     * @return
     */
    private static boolean isYiBanGao(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] >= 2 && h[i + 1 + j * 9] >= 2 && h[i + 2 + j * 9] >= 2){
                    h[i + j * 9] -= 2;
                    h[i + 1+ j * 9] -= 2;
                    h[i + 2 + j * 9] -= 2;
                    int[] copy = h.clone();
                    if(commonHu(copy)){
                        return true;
                    }
                    h[i + j * 9] += 2;
                    h[i + 1+ j * 9] += 2;
                    h[i + 2 + j * 9] += 2;
                }
            }
        }

        return false;
    }

    /**
     * 喜相逢
     * @param pai
     * @param param
     * @return
     */
    private static boolean isXiXiangFeng(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        int[] n = new int[9];
        for(int i = 0; i < 7; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] != 0 && h[i + 1 + j * 9] != 0 && h[i + 2 + j * 9] != 0){
                    h[i + j * 9]--;
                    h[i + 1 + j * 9]--;
                    h[i + 2 + j * 9]--;
                    int[] copy = h.clone();
                    if(commonHu(copy)){
                        n[i]++;
                    }
                    h[i + j * 9]++;
                    h[i + 1 + j * 9]++;
                    h[i + 2 + j * 9]++;
                }
            }
        }

        for(int k : n){
            if(k >= 2){
                return true;
            }
        }
        return false;
    }

    /**
     * 连六
     * @param pai
     * @param param
     * @return
     */
    private static boolean isLianLiu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int i = 0; i < 6; i++){
            for(int j = 0; j < 3; j++){
                if(h[i + j * 9] != 0 && h[i + 1 + j * 9] != 0 && h[i + 2 + j * 9] != 0 && h[i + 3 +  j * 9] != 0 && h[i + 4 + j * 9] != 0 && h[i + 5 + j * 9] != 0){
                    h[i + j * 9]--;
                    h[i + 1 + j * 9]--;
                    h[i + 2 + j * 9]--;
                    h[i + 3 + j * 9]--;
                    h[i + 4 + j * 9]--;
                    h[i + 5 + j * 9]--;
                    int[] copy = h.clone();
                    if(commonHu(copy)){
                        return true;
                    }
                    h[i + j * 9]++;
                    h[i + 1 + j * 9]++;
                    h[i + 2 + j * 9]++;
                    h[i + 3 + j * 9]++;
                    h[i + 4 + j * 9]++;
                    h[i + 5 + j * 9]++;
                }
            }
        }

        return false;
    }

    /**
     * 老少副
     * @param pai
     * @param param
     * @return
     */
    private static boolean isLaoShaoFu(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        // 取出吃
        List<List<Integer>> chi = ((List<List<Integer>>) param.get("chi"));

        for(List<Integer> list : chi){
            h[list.get(0)]++;
            h[list.get(1)]++;
            h[list.get(2)]++;
        }

        for(int j = 0; j < 3; j++){
            if(h[j * 9] != 0 && h[1 + j * 9] != 0 && h[2 + j * 9] != 0 && h[6 +  j * 9] != 0 && h[7 + j * 9] != 0 && h[8 + j * 9] != 0){
                h[j * 9]--;
                h[1 + j * 9]--;
                h[2 + j * 9]--;
                h[6 + j * 9]--;
                h[7 + j * 9]--;
                h[8 + j * 9]--;
                int[] copy = h.clone();
                if(commonHu(copy)){
                    return true;
                }
                h[j * 9]++;
                h[1 + j * 9]++;
                h[2 + j * 9]++;
                h[6 + j * 9]++;
                h[7 + j * 9]++;
                h[8 + j * 9]++;
            }
        }

        return false;
    }

    /**
     * 幺九刻
     * @param pai
     * @param param
     * @return
     */
    private static boolean isYaoJiuKe(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        int menFeng = (Integer) param.get("menFeng") + 27;
        int quanFeng = (Integer) param.get("quanFeng") + 27;

        List<Integer> list = new ArrayList<>(Arrays.asList(0, 8, 9, 17, 18, 26));
        for(int i = 27; i < 31; i++){
            if(i != menFeng && i != quanFeng){
                list.add(i);
            }
        }

        List<Integer> peng = (List<Integer>) param.get("peng");
        List<Integer> mingGang = (List<Integer>) param.get("mingGang");
        List<Integer> anGang = (List<Integer>) param.get("anGang");

        for(int j : peng){
            for(int k : list){
                if(j == k) return true;
            }
        }

        for(int j : mingGang){
            for(int k : list){
                if(j == k) return true;
            }
        }

        for(int j : anGang){
            for(int k : list){
                if(j == k) return true;
            }
        }

        for(int i = 27; i < 31; i++){
            if(h[i] >= 3){
                return true;
            }
        }

        for(int i : list){
            if(h[i] >= 3){
                h[i] -= 3;
                int[] copy = h.clone();
                if(commonHu(copy)){
                    return true;
                }
                h[i] += 3;
            }
        }

        return false;
    }

    /**
     * 明杠
     * @param pai
     * @param param
     * @return
     */
    private static boolean isMingGang(int[] pai, Map<String, Object> param){

        List<Integer> list = (List<Integer>) param.get("mingGang");
        return list.size() > 0;
    }

    /**
     * 缺一门
     * @param pai
     * @param param
     * @return
     */
    private static boolean isQueYiMen(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

        int mCount = 0;
        int bCount = 0;
        int sCount = 0;
        for(int i = 0; i < 27; i++){
            if(h[i] > 0){
                if(i < 9){
                    mCount++;
                }else if(i < 18){
                    bCount++;
                }else{
                    sCount++;
                }
            }
        }

        int count = 0;
        if(mCount != 0) count++;
        if(bCount != 0) count++;
        if(sCount != 0) count++;
        return count == 2;
    }

    /**
     * 无字
     * @param pai
     * @param param
     * @return
     */
    private static boolean isWuZi(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

        for(int i = 27; i < 34; i++){
            if(h[i] != 0){
                return false;
            }
        }

        return true;
    }

    /**
     * 单钓将
     * @param pai
     * @param param
     * @return
     */
    private static boolean isDanDiaoJiang(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

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

        Integer huCard = (Integer) param.get("huCard");
        if(huCard == null){
            return false;
        }

        if(h[huCard] == 2){
            h[huCard] -= 2;
            if(hu(h)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 边张
     * @param pai
     * @param param
     * @return
     */
    private static boolean isBianZhang(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        Integer huCard = (Integer) param.get("huCard");
        if(huCard == null){
            return false;
        }

        if(huCard >= 27){
            return false;
        }

        // 3
        if(huCard % 9 == 2){
            h[huCard]--;
            h[huCard - 1]--;
            h[huCard - 2]--;

            int[] copy = h.clone();
            if(commonHu(copy)){
                h[huCard]--;
                h[huCard + 1]--;
                h[huCard + 2]--;

                copy = h.clone();
                if(commonHu(copy)){
                    return false;
                }else{
                    return true;
                }
            }else{
                return false;
            }
        }else if(huCard % 9 == 6){
            // 7
            h[huCard]--;
            h[huCard + 1]--;
            h[huCard + 2]--;

            int[] copy = h.clone();
            if(commonHu(copy)){
                h[huCard]--;
                h[huCard - 1]--;
                h[huCard - 2]--;

                copy = h.clone();
                if(commonHu(copy)){
                    return false;
                }else{
                    return true;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 坎张
     * @param pai
     * @param param
     * @return
     */
    private static boolean isKanZhang(int[] pai, Map<String, Object> param){

        int[] h = pai.clone();

        Integer huCard = (Integer) param.get("huCard");
        if(huCard == null){
            return false;
        }
        if(huCard >= 27){
            return false;
        }

        if(huCard % 9 == 0 || huCard % 9 == 8){
            return false;
        }

        if (h[huCard] == 2) {
            h[huCard - 1] -= 2;
            h[huCard] -= 2;
            h[huCard + 1] -= 2;
        }else{
            h[huCard - 1] -= 1;
            h[huCard] -= 1;
            h[huCard + 1] -= 1;
        }

        int[] copy = h.clone();
        if(commonHu(copy)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 自摸
     * @param h
     * @param param
     * @return
     */
    private static boolean isZiMo(int[] h, Map<String, Object> param){
        return (boolean) param.get("isZiMo");
    }

    /**
     * 计算番数
     * @param h
     * @param param
     * @return
     */
    public static Map<String, Integer> calculate(int[] h, Map<String, Object> param){

        Map<String, Integer> map = new TreeMap<>();
        if(isDaSiXi(h, param)){
            map.put("大四喜", 88);
        }else if(isDaSanYuan(h, param)){
            map.put("大三元", 88);
        }else if(is13Yao(h)){
            map.put("十三幺", 88);
        }else if(isJiuLianBaoDeng(h, param)){
            map.put("九莲宝灯", 88);
        }else if(isLianQiDui(h, param)){
            map.put("连七对", 88);
        }else if(isLvYiSe(h, param)){
            map.put("绿一色", 88);
        }else if(isSiGang(h, param)){
            map.put("四杠", 88);
        }else if(isXiaoSanYuan(h, param)){
            map.put("小三元", 64);
        }else if(isXiaoSiXi(h, param)){
            map.put("小四喜", 64);
        }else if(isQingYaoJiu(h, param)){
            map.put("清幺九", 64);
        }else if(isYiSeShuangLongHui(h, param)){
            map.put("一色双龙会", 64);
        }else if(isZiYiSe(h, param)){
            map.put("字一色", 64);
        }else if(isSiAnKe(h, param)){
            map.put("四暗刻", 64);
        }else if(isYiSeSiJieGao(h, param)){
            map.put("一色四节高", 48);
        }else if(isYiSeSiTongShun(h, param)){
            map.put("一色四同顺", 48);
        }else if(isYiSeSiBuGao(h, param)){
            map.put("一色四步高", 32);
        }else if(!map.containsKey("十三幺") && isHunYaoJiu(h, param)){
            map.put("混幺九", 32);
        }else if(!map.containsKey("四杠") && isSanGang(h, param)){
            map.put("三杠", 32);
        }else if(!map.containsKey("连七对") && !map.containsKey("连七对") && isQiDui(h)){
            map.put("七对", 24);
        }else if(isQiXingBuKao(h)){
            map.put("七星不靠", 24);
        }else if(isQuanShuangKe(h, param)){
            map.put("全双刻", 24);
        }else if(isQuanDa(h, param)){
            map.put("全大", 24);
        }else if(isQuanZhong(h, param)){
            map.put("全中", 24);
        }else if(!map.containsKey("一色四同顺") && isYiSeSanJieGao(h, param)){
            map.put("一色三节高", 24);
        }else if(!map.containsKey("一色四节高") && !map.containsKey("一色三节高") && isYiSeSanTongShun(h, param)){
            map.put("一色三同顺", 24);
        }else if(isQingLong(h, param)){
            map.put("清龙", 16);
        }else if(isSanSeSanLongHui(h, param)){
            map.put("三色三龙会", 16);
        }else if(isYiSeSanBuGao(h, param)){
            map.put("一色三步高", 16);
        }else if(isQuanBuKao(h)){
            map.put("全不靠", 12);
        }else if(!map.containsKey("大四喜") && !map.containsKey("小四喜") && isSanFengKe(h, param)){
            map.put("三风刻", 12);
        }else if(isSanSeSanTongShun(h, param)){
            map.put("三色三同顺", 12);
        }else if(isSanSeSanJieGao(h, param)){
            map.put("三色三节高", 12);
        }else if(isSanSeSanBuGao(h, param)){
            map.put("三色三步高", 6);
        }

        if(isQuanXiao(h, param)){
            map.put("全小", 24);
        }

        if(!map.containsKey("九莲宝灯") && !map.containsKey("连七对") && !map.containsKey("一色双龙会") && isQingYiSe(h, param)){
            map.put("清一色", 24);
        }

        if(isQuanDaiWu(h, param)){
            map.put("全带五", 16);
        }

        if(isSanAnKe(h)){
            map.put("三暗刻", 16);
        }

        if(!map.containsKey("清幺九") && isSanTongKe(h, param)){
            map.put("三同刻", 16);
        }

        if(isDaYuWu(h, param)){
            map.put("大于五", 12);
        }

        if(isXiaoYuWu(h, param)){
            map.put("小于五", 12);
        }

        if(isHuaLong(h, param)){
            map.put("花龙", 8);
        }

        if(isTuiBuDao(h, param)){
            map.put("推不倒", 8);
        }

        if(isHaiDiLaoYue(h, param)){
            map.put("海底捞月", 8);
        }

        if(isGangShangKaiHua(h, param)){
            map.put("杠上开花", 8);
        }

        if(isQiangGanghu(h, param)){
            map.put("抢杠和", 8);
        }

        if(isMiaoShouHuiChun(h, param)){
            map.put("妙手回春", 8);
        }

        if(!map.containsKey("绿一色") && isHunYiSe(h, param)){
            map.put("混一色", 8);
        }

        if(!map.containsKey("大四喜") && !map.containsKey("四杠") && !map.containsKey("清幺九") && !map.containsKey("字一色") && !map.containsKey("四暗刻") && !map.containsKey("一色四节高") && !map.containsKey("混幺九")
                && !map.containsKey("全双刻") && isPengPengHu(h, param)){
            map.put("碰碰和", 6);
        }

        if(!map.containsKey("十三幺") && !map.containsKey("七星不靠") && !map.containsKey("全不靠") && isWuMenQi(h, param)){
            map.put("五门齐", 6);
        }

        if(!map.containsKey("大三元") && !map.containsKey("小三元") && isShuangJianKe(h, param)){
            map.put("双箭刻", 6);
        }

        if(isYiMingGangYiAnGang(h, param)){
            map.put("一明杠一暗杠", 6 );
        }

        if(!map.containsKey("清幺九") && !map.containsKey("字一色") && !map.containsKey("混幺九") && isQuanDaiYao(h, param)){
            map.put("全带幺", 4);
        }

        if(isQuanQiuRen(h, param)){
            map.put("全求人", 6);
        }else if(!map.containsKey("七对") && !map.containsKey("七星不靠") && !map.containsKey("全不靠") && isBuQiuRen(h, param)){
            map.put("不求人", 4);
        }

        if(isShuangMingGang(h, param)){
            map.put("双明杠", 4);
        }

        if(isHuJueZhang(h, param)){
            map.put("和绝张", 4);
        }

        if(!map.containsKey("大三元") && !map.containsKey("小三元") && !map.containsKey("双箭刻") && isJianKe(h, param)){
            map.put("箭刻", 2);
        }

        if(!map.containsKey("大四喜") && isQuanFengKe(h, param)){
            map.put("圈风刻", 2);
        }

        if(!map.containsKey("大四喜") && isMenFengKe(h, param)){
            map.put("门风刻", 2);
        }

        if(!map.containsKey("九莲宝灯") && !map.containsKey("连七对") && !map.containsKey("十三幺") && !map.containsKey("四暗刻") && !map.containsKey("七对") && !map.containsKey("七星不靠") && !map.containsKey("全不靠") && !map.containsKey("不求人")
        && isMenQianQing(h, param)){
            map.put("门前清", 2);
        }

        if(!map.containsKey("一色双龙会") && !map.containsKey("三色三龙会") && isPingHu(h, param)){
            map.put("平和", 2);
        }

        if(!map.containsKey("一色四同顺") && isSiGuiYi(h, param)){
            map.put("四归一", 2);
        }

        if(!map.containsKey("清幺九") && !map.containsKey("三同刻") && isShuangTongKe(h, param)){
            map.put("双同刻", 2);
        }

        if(!map.containsKey("三暗刻") && !map.containsKey("双暗杠") && isShuangAnKe(h, param)){
            map.put("双暗刻", 2);
        }

        if(!map.containsKey("大四喜") && !map.containsKey("大三元") && !map.containsKey("小四喜") && !map.containsKey("小三元") && !map.containsKey("三杠") && !map.containsKey("四杠") && !map.containsKey("双暗杠") && !map.containsKey("一明杠一暗杠") && isAnGang(h, param)){
            map.put("暗杠", 2);
        }

        if(!map.containsKey("全双刻") && !map.containsKey("全带五") && !map.containsKey("全中") && isDuanYao(h, param)){
            map.put("断幺", 2);
        }

        if(!map.containsKey("一色双龙会") && !map.containsKey("一色四同顺") && !map.containsKey("一色三同顺") && isYiBanGao(h, param)){
            map.put("一般高", 1);
        }

        if(!map.containsKey("三色三龙会") && !map.containsKey("三色三同顺") && !map.containsKey("一色四同顺") && !map.containsKey("一色三同顺") && isXiXiangFeng(h, param)){
            map.put("喜相逢", 1);
        }

        if(!map.containsKey("一色四步高") && !map.containsKey("清龙") && !map.containsKey("一色三步高") && isLianLiu(h, param)){
            map.put("连六", 1);
        }

        if(!map.containsKey("一色双龙会") && !map.containsKey("一色四步高") && !map.containsKey("清龙") && !map.containsKey("三色三龙会") && isLaoShaoFu(h, param)){
            map.put("老少副", 1);
        }

        if(!map.containsKey("九莲宝灯") && !map.containsKey("清幺九") && !map.containsKey("小四喜") && !map.containsKey("字一色") && !map.containsKey("混一色") && !map.containsKey("五门齐") && !map.containsKey("圈粉刻")
                && !map.containsKey("门风刻") && isYaoJiuKe(h, param)){
            map.put("幺九刻", 1);
        }

        if(!map.containsKey("大四喜") && !map.containsKey("大三元") && !map.containsKey("小四喜") && !map.containsKey("小三元") && !map.containsKey("一明杠一暗杠") && !map.containsKey("三杠") && !map.containsKey("四杠") && !map.containsKey("双明杠") && isMingGang(h, param)){
            map.put("明杠", 1);
        }

        if(!map.containsKey("推不倒") && isQueYiMen(h, param)){
            map.put("缺一门", 1);
        }

        if(!map.containsKey("清幺九") && !map.containsKey("一色双龙会") && !map.containsKey("清一色") && !map.containsKey("全大") && !map.containsKey("全中") && !map.containsKey("全小") && !map.containsKey("三色三龙会")
                && !map.containsKey("大于五") && !map.containsKey("小于五") && !map.containsKey("平和") && !map.containsKey("断幺") && isWuZi(h, param)){
            map.put("无字", 1 );
        }

        if(isBianZhang(h, param)){
            map.put("边张", 1);
        }else if(isKanZhang(h, param)){
            map.put("坎张", 1);
        }

        if(isDanDiaoJiang(h, param)){
            map.put("单钓将", 1);
        }

        if(isZiMo(h, param)){
            map.put("自摸", 1);
        }

        if(!map.containsKey("五门齐") && !map.containsKey("幺九刻") && !map.containsKey("碰碰和") && !map.containsKey("门前清") && !map.containsKey("全求人") && !map.containsKey("四归一") && !map.containsKey("三色三同顺")
        && !map.containsKey("一般高") && !map.containsKey("喜相逢") && !map.containsKey("连六") && !map.containsKey("老少副") && !map.containsKey("三色三节高") && !map.containsKey("三同刻") && !map.containsKey("三暗刻")
        && !map.containsKey("双同刻") && !map.containsKey("双暗刻") && !map.containsKey("幺九刻") && !map.containsKey("边张") && !map.containsKey("坎张") && !map.containsKey("单钓将") && !map.containsKey("自摸")
        && !map.containsKey("海底捞月") && !map.containsKey("抢杠和") && !map.containsKey("妙手回春")){
            if(isWuFanHe(h, param)){
                map.put("无番和", 8);
            }
        }

        map.put("花牌", (Integer) param.get("flowerCount"));

        return map;
    }

    /**
     * 判断能不能胡
     * @param pai
     * @return
     */
    public static boolean canHu(int[] pai, Integer card){

        int[] h = pai.clone();
        if(card != null){
            h[card]++;
        }
        // 特殊和型
        if(is13Yao(h)){
            return true;
        }
        if(isQuanBuKao(h)){
            return true;
        }
        if(isQiXingBuKao(h)){
            return true;
        }
        // 一般和型
        if(commonHu(h)){
            return true;
        }
        return false;
    }

    /**
     * 能否左吃
     * @return
     */
    public static boolean canLeftChi(int[] pai, Integer card){

        if(card == null) return false;
        if(card >= 27) return false;
        if(card % 9 > 6) return false;

        int[] h = pai.clone();
        h[card]++;
        if(h[card] != 0 && h[card + 1] != 0 && h[card + 2] != 0) {
            return true;
        }
        return false;
    }

    /**
     * 能否中吃
     * @param pai
     * @param card
     * @return
     */
    public static boolean canMidChi(int[] pai, Integer card){
        if(card == null) return false;
        if(card >= 27) return false;
        if(card % 9 == 0 || card % 9 == 8) return false;

        int[] h = pai.clone();
        h[card]++;
        if(h[card - 1] != 0 && h[card] != 0 && h[card + 1] != 0) {
            return true;
        }
        return false;
    }

    /**
     * 能否右吃
     * @param pai
     * @param card
     * @return
     */
    public static boolean canRightChi(int[] pai, Integer card){
        if(card == null) return false;
        if(card >= 27) return false;
        if(card % 9 < 2) return false;

        int[] h = pai.clone();
        h[card]++;
        if(h[card] != 0 && h[card - 1] != 0 && h[card - 2] != 0) {
            return true;
        }
        return false;
    }

    /**
     * 能否碰
     * @param pai
     * @param card
     * @return
     */
    public static boolean canPeng(int[] pai, Integer card){
        if(card == null){
            return  false;
        }
        return pai[card] == 2;
    }

    /**
     * 能否杠
     * @param pai
     * @param card
     * @return
     */
    public static boolean canGang(int[] pai, Integer card){
        if(card == null){
            for(int k : pai){
                if(k == 4){
                    return true;
                }
            }
            return false;
        }
        return pai[card] == 3;
    }

    /**
     * 洗牌
     * @return
     */
    public static List<Integer> shuffle(){

        Random rand = new Random();

        List<Integer> list = new ArrayList<>();
        // 每种 4 张
        for(int i = 0; i < 34; i++){
            for(int j = 0; j < 3; j++){
                list.add(i);
            }
        }
        // 8 张花牌
        for(int i = 0; i < 8; i++){
            list.add(34);
        }

        for(int i = list.size() - 1; i >= 0; i--){
            swap(list, rand.nextInt(i + 1), i);
        }

        return list;
    }

    /**
     * 判断是否能达到 8 番
     * @param card
     * @param isZiMo
     * @param isGangMoPai
     * @param isQiangGangHu
     * @param playerId
     * @param roomId
     * @return
     */
    public static boolean calculateHu(Integer card, boolean isZiMo, boolean isGangMoPai, boolean isQiangGangHu, Integer playerId, Integer roomId){
        MahJongCache mahJongCache = MahJongCache.getInstance();
        RoomCacheBean roomCacheBean = mahJongCache.getRoomByRoomId(roomId);
        PlayerCacheBean player = mahJongCache.getPlayerByPlayerId(roomId, playerId);

        int count = 0;
        if(card != null){
            for(int k : roomCacheBean.getCardWall()){
                if(k == card){
                    count++;
                }
            }
        }

        int[] h = player.getHandCard();

        Map<String, Object> param = new HashMap<>();
        param.put("isZiMo", isZiMo);
        param.put("isLastCard", roomCacheBean.getCardWall().size() == 0);
        param.put("isGangMoPai", isGangMoPai);
        param.put("isQiangGangHu", isQiangGangHu);
        param.put("isHuJueZhang", card != null && count == 0);
        param.put("quanFeng", roomCacheBean.getQuan());
        param.put("menFeng", mahJongCache.getPlayerIndex(roomCacheBean.getRoomId(), player.getPlayerId()));
        param.put("chi", player.getChis());
        param.put("peng", player.getPengs());
        param.put("mingGang", player.getMingGangs());
        param.put("anGang", player.getAnGangs());
        param.put("huCard", card);
        param.put("flowerCount", player.getFlowerCard());
        Map<String, Integer> map = MahJongUtils.calculate(h, param);

        int point = 0;
        for(Integer p : map.values()){
            point += p;
        }

        if(point >= 8){
            return true;
        }
        return false;
    }

    public static Map<String, Integer> calculateFan(Integer card, Integer menFeng, boolean isZiMo, boolean isGangMoPai, boolean isQiangGangHu, PlayerCacheBean player, RoomCacheBean room){

        // 门风：mahJongCache.getPlayerIndex(room.getRoomId(), player.getPlayerId())

        int count = 0;
        if(card != null){
            for(int k : room.getCardWall()){
                if(k == card){
                    count++;
                }
            }
        }

        int[] h = player.getHandCard();

        Map<String, Object> param = new HashMap<>();
        param.put("isZiMo", isZiMo);
        param.put("isLastCard", room.getCardWall().size() == 0);
        param.put("isGangMoPai", isGangMoPai);
        param.put("isQiangGangHu", isQiangGangHu);
        param.put("isHuJueZhang", card != null && count == 0);
        param.put("quanFeng", room.getQuan());
        param.put("menFeng", menFeng);
        param.put("chi", player.getChis());
        param.put("peng", player.getPengs());
        param.put("mingGang", player.getMingGangs());
        param.put("anGang", player.getAnGangs());
        param.put("huCard", card);
        param.put("flowerCount", player.getFlowerCard());
        Map<String, Integer> map = MahJongUtils.calculate(h, param);
        return map;
    }

    private static void swapPos(List<PlayerCacheBean> list, int i, int j){
        PlayerCacheBean temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    public static void setRandPosition(RoomCacheBean room) {
        Random random = new Random();
        List<PlayerCacheBean> list = room.getList();
        for(int i = 3; i >= 0; i--){
            swapPos(list, i, random.nextInt(i + 1));
        }
    }

    public static void changeZhuang(RoomCacheBean room){
        List<PlayerCacheBean> list = room.getList();
        PlayerCacheBean first = list.remove(0);
        list.add(first);
    }

    /**
     * 换座位
     */
    public static void changePosition(RoomCacheBean room){
        List<PlayerCacheBean> list = room.getList();
        int quan = room.getQuan();

        // 先换庄恢复之前的状态
        changeZhuang(room);

        // 东风圈
        if(quan == 0 || quan == 2){
            swapPos(list, 0, 1);
            swapPos(list, 2, 3);
        }else{
            swapPos(list, 0, 3);
            swapPos(list, 1, 2);
            swapPos(list, 2, 3);
        }
    }

    public static void deal( List<PlayerCacheBean> players, List<Integer> cards){

        // 发牌
        int[] h1 = new int[34];
        int[] h2 = new int[34];
        int[] h3 = new int[34];
        int[] h4 = new int[34];

        for(int i = 0; i < 14; i++) {
            while(cards.get(0) == 34){
                players.get(0).addFlowerCard(1);
                cards.remove(0);
            }
            h1[cards.remove(0)]++;
        }
        players.get(0).setHandCard(h1);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(1).addFlowerCard(1);
                cards.remove(0);
            }
            h2[cards.remove(0)]++;
        }
        players.get(1).setHandCard(h2);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(2).addFlowerCard(1);
                cards.remove(0);
            }
            h3[cards.remove(0)]++;
        }
        players.get(2).setHandCard(h3);

        for(int i = 0; i < 13; i++){
            while(cards.get(0) == 34){
                players.get(3).addFlowerCard(1);
                cards.remove(0);
            }
            h4[cards.remove(0)]++;
        }
        players.get(3).setHandCard(h4);
    }

    public static String mahJongInfo(PlayerCacheBean p, RoomCacheBean r) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\r\n");
        sb.append("牌池: \r\n");
        sb.append(getCardsInfo(r.getCardPool()));
        sb.append("您的手牌: \r\n");
        sb.append(getHandCardInfo(p.getHandCard()) + "\r\n");
        sb.append("您的碰:\r\n");
        sb.append(getCardsInfo(p.getPengs()));
        sb.append("您的暗杠:\r\n");
        sb.append(getCardsInfo(p.getAnGangs()));
        sb.append("您的明杠:\r\n");
        sb.append(getCardsInfo(p.getMingGangs()));
        sb.append("您的花牌数量: " + p.getFlowerCard() + "\r\n");
        sb.append("=================================================================");
        return sb.toString();
    }

    /**
     * 返回玩家所有能够使用的操作
     * @param isHu
     * @param isChi
     * @param isPeng
     * @param isGang
     * @return
     */
    public static String getOptions(boolean isPlay, boolean isHu, boolean isChi, boolean isPeng, boolean isGang){

        boolean isPass = false;
        if(isChi || isPeng || isGang){
            isPass = true;
        }

        StringBuilder sb = new StringBuilder();
        if(!isPlay && !isHu && !isChi && !isPeng && !isGang && !isPass){
            sb.append("您无可使用的操作");
        }else{
            sb.append("=================================\r\n");
            sb.append("您可以使用的操作如下：");
            if(isPlay){
                sb.append("[play:[num]]:打一张牌\r\n");
            }if(isHu){
                sb.append("[hu]:胡\r\n");
            }
            if(isGang){
                sb.append("[gang]杠\r\n");
            }
            if(isPeng){
                sb.append("[peng]碰\r\n");
            }
            if(isChi){
                sb.append("[chi:0、1、2]: 左、中、右吃\r\n");
            }
            if(isPass){
                sb.append("[pass]过\r\n");
            }
            sb.append("=================================");
        }
        return sb.toString();
    }

    /**
     * 获取玩家手牌的信息
     * @param h
     * @return
     */
    private static String getHandCardInfo(int[] h){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < h.length; i++){
            if(h[i] != 0){
                for(int j = 0; j < h[i]; j++){
                    sb.append(getCardInfo(i));
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获得牌池的信息
     * @param cardPool
     * @return
     */
    private static String getCardsInfo(List<Integer> cardPool) {
        if(cardPool.size() == 0){
            return "无";
        }
        StringBuilder sb = new StringBuilder();
        // 每二十张牌显示一行
        for(int i = 0; i < cardPool.size(); i += 20){
            for(int j = i; j < i + 20; j++){
                if(cardPool.get(j) < 9){
                    sb.append("|" + oneToNight(cardPool.get(j)) + "万|");
                }else if(cardPool.get(j) < 18){
                    sb.append("|" + oneToNight(cardPool.get(j) % 9) + "饼|");
                }else if(cardPool.get(j) < 27){
                    sb.append("|" + oneToNight(cardPool.get(j) % 9) + "条|");
                }else{
                    sb.append("|" + numToTile(cardPool.get(j) - 27) + "|");
                }
                if(j == cardPool.size() - 1){
                    break;
                }
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public static String getCardInfo(int card){
        if(card < 9){
            return "|" + oneToNight(card) + "万|";
        }else if(card < 18){
            return "|" + oneToNight(card % 9) + "饼|";
        }else if(card < 27){
            return "|" + oneToNight(card % 9) + "条|";
        }else{
            return "|" + numToTile(card - 27) + "|";
        }
    }

    /**
     * 将数字转化为大写数字
     * @param num
     * @return
     */
    private static String oneToNight(int num){
        switch(num) {
            case 0:
                return "一";
            case 1:
                return "二";
            case 2:
                return "三";
            case 3 :
                return "四";
            case 4:
                return "五";
            case 5:
                return "六";
            case 6 :
                return "七";
            case 7:
                return "八";
            case 8:
                return "九";
            default:
                return null;
        }
    }

    /**
     * 将数字转化为东西南北中发白
     * @param num
     * @return
     */
    private static String numToTile(int num){
        switch(num) {
            case 0:
                return "东";
            case 1:
                return "南";
            case 2:
                return "西";
            case 3 :
                return "北";
            case 4:
                return "中";
            case 5:
                return "发";
            case 6 :
                return "白";
            default:
                return null;
        }
    }

    public static String fangInfo(Map<String, Integer> map){

        StringBuilder sb = new StringBuilder();
        sb.append("=======================================\r\n");
        sb.append("您的番数:\r\n");
        for(Map.Entry<String,Integer> entry : map.entrySet()){
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
        }
        sb.append("=======================================");
        return sb.toString();
    }

    private static void swap(List<Integer> list, int l, int r){
        Integer temp = list.get(l);
        list.set(l, list.get(r));
        list.set(r, temp);
    }

    public static void clearPlayerState(PlayerCacheBean player) {
        player.setAnGang(false);
        player.setMingGang(false);
        player.setGangMoHu(false);
        player.setPeng(false);
        player.setChi(false);
        player.setTianHu(false);
        player.setQiangGangHu(false);
        player.setZiMo(false);
        player.setFanPao(false);
        player.setNeedPlay(false);
    }

    public static void clearRoomState(RoomCacheBean room){
        room.setWhoChi(-1);
        room.setWhoMingGang(-1);
        room.setWhoPlay(-1);
        room.setWhoPeng(-1);
        room.setWhoHu(-1);
    }

    public static Integer countFan(Map<String, Integer> map){
        int count = 0;
        for(Map.Entry<String,Integer> entry : map.entrySet()){
            count += entry.getValue();
        }
        return count;
    }

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
        pai[6] = 0;
        pai[7] = 0;
        pai[8] = 0;

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
        pai[23] = 0;
        pai[24] = 0;
        pai[25] = 0;
        pai[26] = 0;

        // 东西南北中发白
        pai[27] = 0;
        pai[28] = 0;
        pai[29] = 0;
        pai[30] = 0;
        pai[31] = 0;
        pai[32] = 2;
        pai[33] = 0;

        Map<String, Object> map = new HashMap<>();
        List<Integer> peng = new ArrayList<>();
        List<Integer> mingGang = new ArrayList<>();
        List<Integer> anGang = new ArrayList<>();
        List<List<Integer>> chi = new ArrayList<>();

        List<Integer> l1 = new ArrayList<>();
        List<Integer> l2 = new ArrayList<>();
        List<Integer> l3 = new ArrayList<>();
        List<Integer> l4 = new ArrayList<>();


        l1.add(19);
        l1.add(20);
        l1.add(21);

        l2.add(19);
        l2.add(20);
        l2.add(21);

        peng.add(29);
        peng.add(30);

        mingGang.add(27);
        mingGang.add(28);

        map.put("chi", chi);
        map.put("peng", peng);
        map.put("mingGang", mingGang);
        map.put("anGang", anGang);
        map.put("menFeng", 0);
        map.put("quanFeng", 0);
        map.put("isLastCard", false);
        map.put("isZiMo", true);
        map.put("isGangMoPai", false);
        map.put("isQiangGangHu", false);
        map.put("isHuJueZhang", false);
        map.put("huCard", 32);
        map.put("flowerCount", 2);
//        System.out.println(calculate(pai, map));

        System.out.println(shuffle());
    }
}
