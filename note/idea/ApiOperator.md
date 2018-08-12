

JsonObject jsonObj = ApiOperator 
						.get("/ssmp/endeca/search")//返回一个RequestEntityBuilder
						.dynamicVersion()
						.param("keyword","australia")
						.cookie("cookieName","cookieValue")
						.header("headerName","headerValue")	
						.request()//返回一个responseEntityHolder()
						.ifNotOk(Object)//如果不是200、或者有ApiException出现（就不会抛出异常了）就使用该 object 作为alternative请求相应的内容
						.addConsumers(new Consumer(){})
						.toJsonObj();

创建一个ApiResponseHolder,下面有几个子类：ErrorResponseHolder(请求中发生了异常) OkResponseHolder(200),DefaultResponseHolde(非200)

ApiResponseHolder是一个abstract类，有方法 newApiResponseHolder(CloseableClientHttpResponse)根据状态码来决定返回那个子类的实例。需要关闭CloseableClientHttpResponse
如果请求中抛出了异常，就直接返回 ErrorResponseHolder类的实例。

ApiResponseHolder封装了返回的CloseableClientHttpResponse中各种属性。他的子类里面提供了业务逻辑方法

在ApiContext中有方法： ApiResponseHolder request(){
	
			ApiResponseHolder.newApiResponseHolder(this.requestEntity);//在方法newApiResponseHolder里面调用请求
			
}
在ApiResponseHolder里面会维护一个RequestEntity的引用


//在apiServlet中需要
ApiOperator.dynamic().dynamicVersion().all().request().isOk("Request remote resources failed with ${apiPath}").response();//"在请求参数中必须要有一个叫做apiPath的参数"

//模拟请求后台的blogs
JsonObject jsonObj = ApiOperator.get("/blog/blogs").request().toJsonObj();//如果返回的结果不是json格式或者说不是jsonObject，抛出ApiException.

//模拟blog新增event触发时添加blogid的请求
ApiOperator
.post("/blog/post")
.param("blogId","12345677")
.request()
.isOK("Create a new post's id failed, Please contact admin to sovle this issue!");//isOK()方法断言请求一定返回200 如果你不需要response返回的内容 ，请务必使用isOk()方法

//模拟用户登录的请求
ApiOperator.post("/blog/login").param("username","mockname").param("password","xxxx").request().response();

//staitstic 统计使用ApiOperator发出请求中，不是200的请求，并定时的写回到AEM节点中，方便查看。

// 创建一个exception: ApiException extends RuntimeException 当在请求准备、中、返回结果处理：发生的一切异常都会抛出该异常类。


//在上述的fluent方法都不会抛出受检异常，也就是说说当出现请求失败（比如 host error）会抛出 ApiException（运行时异常）


public final class ApiOperator{

	RequestEntityBuilder get(String url);

	RequestEntityBuilder post(String url);

	RequestEntityBuilder dynamic();

	RequestStat stat();

}

class RequestStat{

	class RequestRecord{

	}
}

interface RequestEntityBuilder{

	RequestEntityBuilder dynamicVersion();

	RequestEntityBuilder param(String name,Object value);//value 是直接toString();

	RequestEntityBuilder header(String name,String... value);

	RequestEntityBuilder cookie(String name,String value);

	RequestEntityBuilder all();

	RequestEntityBuilder consumer(Consumer<ApiResponseHolder>... consumers);

	RequestEntityBuilder ifNotOk(Object object);

	ApiResonseProcessor request();

}

interface ApiResonseProcessor{

	ApiResonseProcessor isOk(String errorMsg);
	
	ApiResonseProcessor noError(String errorMsg);

	JsonObject jsonObj();

	JsonArray jsonArray();

	void response();

}

abstract class ApiResponseHolder implements ApiResonseProcessor{

	RequestEntity requestEnity;

	int status;

	MediaType mediaType;

	String responseContent;

	MultiValueMap headers;

	List<Cookie> cookies;

	Object alternative;

	List<Consumer<ApiResponseHolder>> consumers;


	ApiResonseProcessor newApiResponseHolder(RequestEntity requestEnity){
		this.requestEnity = requestEnity;
		return this.initApiResponseHolder();
	}

	ApiResonseProcessor initApiResponseHolder(){

		ApiResonseProcessor apiResponseHolder;

		try(CloseableClientHttpResponse response = this.requestEnity.request();){

		}catch(Throwable t){
			apiResponseHolder= new ErrorApiResponseHolder(t);
		}
		this.doConsumers(apiResponseHolder);
		return apiResponseHolder;
	}

	private ApiResponseHolder extract(CloseableClientHttpResponse response){
		//获取response status来决定返回的是OkApiResponseHolder/DefaultResponseHolde
	}

	private void doConsumers(ApiResponseHolder apiResponseHolder){

	}

	String parseMsg(String msg);//解析isOk("${apiPath}")等特定的字符


}

class OkApiResponseHolder extends ApiResponseHolder{


}

class DefaultApiResponseHolder extends ApiResponseHolder{


}


class ErrorApiResponseHolder extends ApiResponseHolder{

	//如果没有设置ifNotOk(),全部抛出ApiException

}

public class ApiException extends RuntimeException{

}


上面的都只以接口的形式暴露出来，不把实际的实现类暴露出来，用户只需要知道ApiOperator这个类够了。
大概需要的接口有：RequestEntityBuilder 和 ApiResonseProcessor，然后把默认的实现类都隐藏起来。










