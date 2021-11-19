package com.crud.test;

import com.crud.bean.Employee;
import com.github.pagehelper.PageInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**使用Spring测试模块提供的测试请求的功能，测试crud请求的正确性
 * @author smile
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml","classpath:spring-mvc.xml"})
public class MvcTest {

    /*传入SpringMVC的IOC*/
    @Autowired
    WebApplicationContext context;

    /*虚拟mvc请求，获取到处理结果*/
    MockMvc mockMvc;

    @Before
    public void init(){
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testPage() throws Exception {
        //模拟请求拿到返回值
        MvcResult pn = mockMvc.perform(MockMvcRequestBuilders.get("/emps").param("pn", "1")).andReturn();

        //请求成功之后，请求域中会有pageInfo：取出pageInfo进行验证
        MockHttpServletRequest request = pn.getRequest();
        PageInfo attribute = (PageInfo) request.getAttribute("pageInfo");
        System.out.println("当前页码："+attribute.getPageNum());
        System.out.println("总页码："+attribute.getPages());
        System.out.println("总记录数："+attribute.getTotal());
        System.out.println("连续显示的页码：");
        int[] nums = attribute.getNavigatepageNums();
        for (int i : nums){
            System.out.println(""+i);
        }

        //获取的员工数据
        List<Employee> list = attribute.getList();

        for (Employee employee: list) {
            System.out.println("ID:"+employee.getEmpId()+"===>Name"+employee.getEmpName()+"===>email"+employee.getEmail());
        }
    }

}
