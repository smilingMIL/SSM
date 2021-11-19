package com.crud.controller;

import com.crud.bean.Employee;
import com.crud.bean.Msg;
import com.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**处理员工CRUD请求
 * @author smile
 */
@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;


    /**查询员工数据
     * 导入json包
     * @param pn
     * @param model
     * @return
     */
    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn",defaultValue = "1") Integer pn, Model model){
        //这不是分页查询：
        //引入PageHelper分页插件
        //在查询之前只需要调用：
        PageHelper.startPage(pn,5);//要查第几页，每页多少条数据
        //startPage后面紧跟着的这个查询就是分页查询->
        List<Employee> emps = employeeService.getAll();

        //使用PageInfo包装查询以后的结果，只需要将pageInfo交给页面就行了
        //封装了详细的分页详细，包括有我们查询出来的数据
        PageInfo page = new PageInfo(emps,5);//连续显示5页，1,2,3,4,5 / 2,3,4,5,6

        return Msg.success().add("pageInfo",page);
    }


    /**
     * 添加员工
     * 1.支持JSR303校验
     * 2.导入Hibernate-Validator包
     */
    @RequestMapping(value = "/emp",method = RequestMethod.POST)
    @ResponseBody
    //@Valid：表示需要校验,BindingResult result：表示封装校验结果
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
        if(result.hasErrors()){
            //校验失败,应该返回失败，在模态框中显示校验失败的信息
            Map<String ,Object> map = new HashMap<String, Object>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            for (FieldError fieldError: fieldErrors) {
                map.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            return Msg.fail().add("errorFields",map);
        }else{
            employeeService.saveEmp(employee);
            return Msg.success();
        }
    }

    /**
     * 检查用户名是否可用
     * @param empName
     * @return
     */
    @ResponseBody
    @RequestMapping("/checkUser")
    public Msg checkUser(@RequestParam("empName") String empName){
        //先判断用户名是否合法表达式
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\\u2E80-\\u9FFF]{2,5})";
        if(!empName.matches(regx)){
            return Msg.fail().add("va_msg","用户名格式不正确！2-5中文/6-16英文数字组合。");
        }

        //数据库用户名的重复校验
        boolean b =  employeeService.checkUser(empName);
        if(b){
            return Msg.success();
        }else{
            return Msg.fail().add("va_msg","用户名不可用");
        }
    }


    /**
     * 根据id查员工
     * @return
     */
    @RequestMapping(value = "/emp/{id}",method = RequestMethod.GET)
    @ResponseBody
    public Msg getEmp(@PathVariable("id") Integer id){

        Employee employee = employeeService.getEmp(id);

        return Msg.success().add("emp",employee);
    }


    /**如果直接发送ajax=PUT，处理路径上的数据，其他数据都为null
     *
     * 问题：请求体有数据，但是Employee对象封装不上*
     *
     * 原因：tomcat问题，
     *      1.将请求体中的数据，封装map。
     *      2.request.getParameter("empName")就会从这个map取值
     *      3.SpringMVC封装POJO的对象
     *              会把POJO中每个属性的值，request.getParameter("email)；
     *
     * AJAX发送put请求引发的血案；
     *      PUT请求：请求体的数据，request.getParameter("xx")拿不到
     *          tomcat一看是put请求就不会封装请求体的数据为map，只有post请求才能封装请求体为map
     *
     * 解决方案：
     * ！！！要能直接发送PUT之类的请求还要封装请求体中的数据
     * 在web.xml中filter中配置HttpPutFormContentFilter：
     * 作用将请求体中的数据解析包装成一个map。
     * request被重新包装，request.getParameter()被重写，就会从自己封装的map中取数据。
     *
     * 更新员工信息
     */
    @RequestMapping(value = "/emp/{empId}",method = RequestMethod.PUT)
    @ResponseBody
    public Msg saveEmp(Employee employee){

        employeeService.updateEmp(employee);
        return Msg.success();
    }


    /**
     * 单个/批量 二合一
     * 批量删除：1-2-3
     * 单个删除：1
     *
     * 删除员工
     * @param
     * @return
     */
    @RequestMapping(value = "/emp/{ids}",method = RequestMethod.DELETE)
    @ResponseBody
    public Msg deleteEmp(@PathVariable("ids")String ids){
        //判断是否包含“-”
        /*批量删除*/
        if(ids.contains("-")){
            List<Integer> del_ids = new ArrayList<Integer>();
            String[] str_id = ids.split("-");
            //组装id的集合
            for (String string:str_id){
                del_ids.add(Integer.parseInt(string));
            }
            employeeService.deleteBatch(del_ids);
        }else{
            /*单个删除*/
            int id = Integer.parseInt(ids);
            employeeService.deleteEmp(id);
        }

        return Msg.success();
    }



    /**
     * 查询员工数据(分页查询)
     */
    //@RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn",defaultValue = "1") Integer pn, Model model){

        //这不是分页查询：
        //引入PageHelper分页插件
        //在查询之前只需要调用：
        PageHelper.startPage(pn,5);//要查第几页，每页多少条数据
        //startPage后面紧跟着的这个查询就是分页查询->
        List<Employee> emps = employeeService.getAll();

        //使用PageInfo包装查询以后的结果，只需要将pageInfo交给页面就行了
        //封装了详细的分页详细，包括有我们查询出来的数据
        PageInfo page = new PageInfo(emps,5);//连续显示5页，1,2,3,4,5 / 2,3,4,5,6

        model.addAttribute("pageInfo",page);//在.list取出每个员工信息

        return "list";
    }



}
