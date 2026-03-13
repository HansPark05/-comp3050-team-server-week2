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
Arindam Biswas |	  GITHUB DOCUMENTATION |   ADD UPDATE README FILE ACCORDING TO THE PROGRESS AND CURRENT STATE OF PROJECT
Team Member 2  |                         |  
Team Member 3	 |                         |  
Team member 4  |                         |
team member 5  |                         |

How to Contribute

We follow a Fork and Pull Request workflow to manage contributions.

1. Fork the Repository

Click the Fork button on the GitHub repository to create your own copy.

2. Clone Your Fork

3. Create a New Branch


Example:

git checkout -b add-new-endpoint
4. Make Your Changes

Add new features, fix bugs, or update documentation.

5. Commit Your Changes
git add .
git commit -m "Added new API endpoint"
6. Push Your Branch
git push origin feature-name
7. Create a Pull Request

Go to the original repository on GitHub and create a Pull Request (PR) from your forked branch.

Your repository leader will then review and merge.
