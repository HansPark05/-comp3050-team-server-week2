COMP3050
Available Endpoints

The server currently exposes the following API endpoints:

/hello

Handled by HelloHandler

Method: GET
Response Type: application/json

Example Response:

{
  "message": "Hello from COMP3050!"
}

Description:
This endpoint returns a simple greeting message in JSON format to confirm that the server is running correctly.

/test

Handled by MyHandler

Method: GET
Response Type: application/json

Example Response:

{
  "name": "Japan",
  "gold": 27,
  "silver": 14,
  "bronze": 17,
  "total": 58
}

Description:
This endpoint returns a JSON object containing Olympic medal statistics for a country. It also demonstrates CORS support for cross-origin requests.




Team Members and Roles
   Name	              Role	                     Responsibilities
Arindam Biswas |	                	   |   
Team Member 2  |                       |  
Team Member 3	 |                       |  
Hanseong Park  |    Team Manager                   | Make a repo , Create CI/CD Pipeline
team member 5  |                       |
