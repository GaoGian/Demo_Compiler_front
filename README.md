# Demo_Compiler_front
《编译原理》（机械工业出版社）--编译器前段

#示例代码
{
	int i; int j; float v; float x; float[100] a;
	while(true){
		do i=i+1; while(a[i]<v);
		do j=j-1; while(a[j]>v);
		if(i>=j) break;
		x=a[i]; a[i]=a[j]; a[j]=x;
	}
}


// TODO 下一步计划
1、输出词法分析结果 Token
2、