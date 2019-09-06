#include <stdio.h>
#include <string.h>
#include <stdlib.h>

double cal(char *start, char *end, int x)
{
	char *s = start, *ps = NULL;
	char op = '+', ch;
	int count = 0;
	double curres = 0.0, res = 0.0, num = 0.0;
	while (s <= end)
	{
		ch = *s;
		if (ch == 'x' || ch == 'X')
		{
			num = (double)x;
		}
		if (ch >= '0' && ch <= '9')
		{
			num = num * 10 + (ch - '0');
		}
		else if (ch == '(')
		{
			ps = s;
			count = 0;
			while (ps <= end)
			{
				if (*ps == '(')
					count++;
				else if (*ps == ')')
					count--;
				if (count == 0)
					break;
				ps++;
			}
			num = cal(s + 1, ps - 1, x);
			//printf("num = %d\n", num);
			s = ps;
		}
		if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || s == end)
		{
			switch(op)
			{
			case '+':
				curres += num;
				break;
			case '-':
				curres -= num;
				break;
			case '*':
				curres *= num;
				break;
			case '/':
				curres /= num;
				break;
			}
			if (ch == '+' || ch == '-' || s == end)
			{
				//printf("curres = %d\n", curres);
				res += curres;
				curres = 0;
			}
			op = ch;
			num = 0;
		}
		s++;
	}
	return res;
}

char *mycal(char *cal_str, char data[], double real_data[], int data_size, int data_count)
{
	int count = 0, i, j, every_size, size_now = 0;
	char *cal_start = cal_str, *cal_end = NULL, *p = NULL;
	int num = 0;
	every_size = data_size / data_count;
	for (i = 0; i < data_count; i++)
	{
		cal_end = strchr(cal_start, '_');
		if (cal_end == NULL)
		{
			printf("cal_str error\n");
			return NULL;
		}
		cal_end--;
		
		p = cal_start;
		while (p <= cal_end)
		{
			printf("%c", *p);
			p++;
		}
		printf(" ");
		
		num = 0;
		for (j = 0; j < every_size; j++)
		{
			num = (num << 8) + data[size_now + j];
			//printf("num = %d, %d\n", num, (int)data[size_now + j]);
		}
		real_data[i] = cal(cal_start, cal_end, num);
		printf("x = %d, res = %lf\n", num, real_data[i]);
		
		cal_start = cal_end + 2;
		size_now += every_size;
	}
	return cal_start;
}

/*int main(void)
{
	char str[] = "(1+(4+5+2)-3)*(6+8)";
	int res;
	char *ptail = str;
	while(ptail != NULL)
	{
		if (*(ptail + 1) == '\0')
		{
			break;
		}
		ptail++;
	}
	//printf("*ptail = %d\n", *ptail);
	res = cal(str, ptail);
	printf("res = %d\n", res);
	return 0;
}*/
